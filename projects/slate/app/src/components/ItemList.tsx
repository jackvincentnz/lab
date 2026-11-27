import { useState, type ReactNode } from "react";
import {
  closestCenter,
  DndContext,
  KeyboardSensor,
  MeasuringStrategy,
  PointerSensor,
  useDroppable,
  useSensor,
  useSensors,
  type Collision,
  type CollisionDetection,
  type DragEndEvent,
  type DragMoveEvent,
  type KeyboardCoordinateGetter,
  type UniqueIdentifier,
} from "@dnd-kit/core";
import {
  SortableContext,
  verticalListSortingStrategy,
} from "@dnd-kit/sortable";
import { Badge, Box, Group, List, Stack, Text } from "@mantine/core";
import type { Item, ItemStatus, MoveItemInput } from "../api";
import { useHoldListUpdates } from "../ListUpdates";
import classes from "./ItemList.module.css";
import { SortableItemRow } from "./ItemRow";
import { ListHeading } from "./ListHeading";

const IN_PROGRESS_LIMIT = 3;

const IN_PROGRESS_GROUP = "in-progress-group";

const NEXT_GROUP = "next-group";

type ItemGroupId = typeof IN_PROGRESS_GROUP | typeof NEXT_GROUP;

interface Visit {
  id: UniqueIdentifier;
  group: ItemGroupId;
}

interface ItemListProps {
  items: Item[];
  disabled: boolean;
  openId: string | undefined;
  onMove(move: MoveItemInput, onSettled: () => void): boolean;
  onOpen(item: Item): void;
  onStatusChange(
    item: Item,
    status: ItemStatus,
    onSettled?: () => void,
  ): boolean;
}

export function ItemList({
  items,
  disabled,
  openId,
  onMove,
  onOpen,
  onStatusChange,
}: ItemListProps) {
  const [droppedOrder, setDroppedOrder] = useState<Item[]>();
  const [visit, setVisit] = useState<Visit>();
  const [dragging, setDragging] = useState(false);
  useHoldListUpdates(dragging);
  const shownItems = visited(droppedOrder ?? items, visit);
  const inProgress = shownItems.filter(isInProgress);
  const next = shownItems.filter((item) => !isInProgress(item));
  const sensors = useSensors(
    useSensor(PointerSensor),
    useSensor(KeyboardSensor, { coordinateGetter: nextTarget }),
  );

  function show(changed: boolean, order: Item[]) {
    if (changed) {
      // dnd-kit clears the drag offset on drop, so without the new order in the same render the row jumps back first.
      setDroppedOrder(order);
    }
  }

  // dnd-kit makes room beside the row an item is over, so a visit to the other group is reported as over the item itself to keep its rank's place.
  const collide: CollisionDetection = (args) => {
    const home = groupOf(items.find((item) => item.id === args.active.id));
    const group = nearestGroup(args);
    if (group !== undefined && group !== home) {
      return [{ id: args.active.id, data: { visiting: group } }];
    }
    const homeIds = new Set<UniqueIdentifier>(
      items.filter((item) => groupOf(item) === home).map((item) => item.id),
    );
    return closestCenter({
      ...args,
      droppableContainers: args.droppableContainers.filter((container) =>
        homeIds.has(container.id),
      ),
    });
  };

  function follow({ active, collisions }: DragMoveEvent) {
    const group = visiting(collisions);
    if (group !== visit?.group) {
      setVisit(group === undefined ? undefined : { id: active.id, group });
    }
  }

  function drop({ active, over, collisions }: DragEndEvent) {
    setVisit(undefined);
    setDragging(false);
    const moving = items.find((item) => item.id === active.id);
    if (moving === undefined) {
      return;
    }
    const settled = () => setDroppedOrder(undefined);
    const group = visiting(collisions);

    if (group !== undefined) {
      show(
        onStatusChange(moving, statusIn(group), settled),
        visited(items, { id: moving.id, group }),
      );
      return;
    }

    const neighbour = items.find((item) => item.id === over?.id);
    if (neighbour === undefined || neighbour === moving) {
      return;
    }
    const position =
      items.indexOf(moving) < items.indexOf(neighbour) ? "after" : "before";
    show(
      onMove({ id: moving.id, neighbourId: neighbour.id, position }, settled),
      placed(items, moving, neighbour, position),
    );
  }

  function rows(group: Item[]) {
    return group.map((item) => (
      <SortableItemRow
        key={item.id}
        item={item}
        rank={shownItems.indexOf(item) + 1}
        disabled={disabled}
        open={item.id === openId}
        onOpen={() => onOpen(item)}
        onStatusChange={(status) => onStatusChange(item, status)}
      />
    ));
  }

  return (
    <DndContext
      sensors={sensors}
      collisionDetection={collide}
      measuring={{ droppable: { strategy: MeasuringStrategy.Always } }}
      onDragStart={() => setDragging(true)}
      onDragMove={follow}
      onDragOver={follow}
      onDragEnd={drop}
      onDragCancel={() => {
        setVisit(undefined);
        setDragging(false);
      }}
    >
      <Stack gap="lg">
        <ItemGroup
          id={IN_PROGRESS_GROUP}
          name="In progress"
          items={inProgress}
          empty="Drag an item here when you start it"
          className={classes["inProgress"]}
          overLimit={inProgress.length > IN_PROGRESS_LIMIT}
          heading={
            <Group gap="sm">
              <ListHeading>In progress · {inProgress.length}</ListHeading>
              {inProgress.length > IN_PROGRESS_LIMIT && (
                <Badge color="orange" variant="light">
                  Over your limit of {IN_PROGRESS_LIMIT}
                </Badge>
              )}
            </Group>
          }
        >
          {rows(inProgress)}
        </ItemGroup>
        <ItemGroup
          id={NEXT_GROUP}
          name="Next"
          items={next}
          empty="Drag an item here to stop it"
          heading={<ListHeading>Next</ListHeading>}
        >
          {rows(next)}
        </ItemGroup>
      </Stack>
    </DndContext>
  );
}

interface ItemGroupProps {
  id: ItemGroupId;
  name: string;
  items: Item[];
  empty: string;
  heading: ReactNode;
  className?: string;
  overLimit?: boolean;
  children: ReactNode[];
}

function ItemGroup({
  id,
  name,
  items,
  empty,
  heading,
  className,
  overLimit,
  children,
}: ItemGroupProps) {
  const { setNodeRef } = useDroppable({
    id,
    data: { filled: items.length > 0 },
  });

  return (
    <Stack
      ref={setNodeRef}
      className={className}
      mod={{ "over-limit": overLimit }}
      gap="xs"
      data-group
    >
      {heading}
      {items.length === 0 ? (
        <Box className={classes["dropZone"]} data-empty-group>
          <Text size="sm" c="dimmed">
            {empty}
          </Text>
        </Box>
      ) : (
        <SortableContext items={items} strategy={verticalListSortingStrategy}>
          <List
            type="ordered"
            listStyleType="none"
            spacing={6}
            aria-label={name}
            styles={{ itemWrapper: { display: "block" } }}
          >
            {children}
          </List>
        </SortableContext>
      )}
    </Stack>
  );
}

// dnd-kit's own getter also targets a group, which spans its rows, so a key press could skip past them.
const nextTarget: KeyboardCoordinateGetter = (
  event,
  { context: { active, collisionRect, droppableRects, droppableContainers } },
) => {
  if (event.code !== "ArrowUp" && event.code !== "ArrowDown") {
    return undefined;
  }
  event.preventDefault();
  if (active === null || collisionRect === null) {
    return undefined;
  }
  const down = event.code === "ArrowDown";
  const [next] = droppableContainers
    .getEnabled()
    .filter((container) => container.data.current?.["filled"] !== true)
    .flatMap((container) => droppableRects.get(container.id) ?? [])
    .filter((rect) =>
      down ? rect.top > collisionRect.top : rect.top < collisionRect.top,
    )
    .sort((a, b) => (down ? a.top - b.top : b.top - a.top));
  return next && { x: next.left, y: next.top };
};

function nearestGroup({
  collisionRect,
  droppableRects,
}: Parameters<CollisionDetection>[0]): ItemGroupId | undefined {
  const middle = collisionRect.top + collisionRect.height / 2;
  const [nearest] = ([IN_PROGRESS_GROUP, NEXT_GROUP] as const)
    .flatMap((group) => {
      const rect = droppableRects.get(group);
      return rect
        ? [
            {
              group,
              distance: Math.max(rect.top - middle, middle - rect.bottom, 0),
            },
          ]
        : [];
    })
    .sort((a, b) => a.distance - b.distance);
  return nearest?.group;
}

function visiting(collisions: Collision[] | null): ItemGroupId | undefined {
  return collisions?.[0]?.data?.["visiting"] as ItemGroupId | undefined;
}

function visited(order: Item[], visit: Visit | undefined): Item[] {
  if (visit === undefined) {
    return order;
  }
  return order.map((item) =>
    item.id === visit.id ? { ...item, status: statusIn(visit.group) } : item,
  );
}

function groupOf(item: Item | undefined): ItemGroupId {
  return item !== undefined && isInProgress(item)
    ? IN_PROGRESS_GROUP
    : NEXT_GROUP;
}

function statusIn(group: ItemGroupId): ItemStatus {
  return group === IN_PROGRESS_GROUP ? "in_progress" : "todo";
}

function isInProgress(item: Item) {
  return item.status === "in_progress";
}

function placed(
  items: Item[],
  moved: Item,
  neighbour: Item,
  position: MoveItemInput["position"],
): Item[] {
  const order = items.filter((item) => item.id !== moved.id);
  const at = order.indexOf(neighbour) + (position === "after" ? 1 : 0);
  order.splice(at, 0, moved);
  return order;
}
