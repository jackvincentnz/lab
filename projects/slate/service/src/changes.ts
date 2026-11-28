import { EventEmitter } from "node:events";
import { type Store } from "./operations.js";

export class ListChanges extends EventEmitter<{ change: [] }> {}

export function announceChanges(store: Store, changes: ListChanges): Store {
  function announced<T>(result: T): T {
    changes.emit("change");
    return result;
  }

  return {
    list: (input) => store.list(input),
    add: (name, description, sources) =>
      announced(store.add(name, description, sources)),
    update: (input) => announced(store.update(input)),
    move: (input) => announced(store.move(input)),
    delete: (id) => announced(store.delete(id)),
    restore: (id) => announced(store.restore(id)),
    changes: (id) => store.changes(id),
  };
}
