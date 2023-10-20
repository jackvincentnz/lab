import { IconListCheck, IconNotebook } from "@tabler/icons-react";
import {
  createStyles,
  Group,
  Text,
  ThemeIcon,
  UnstyledButton,
} from "@mantine/core";

interface MainLinkProps {
  icon: React.ReactNode;
  color: string;
  label: string;
  href: string;
  dataTest?: string;
}

const useStyles = createStyles(() => ({
  link: {
    textDecoration: "none",
  },
}));

function MainLink({ icon, color, label, href, dataTest }: MainLinkProps) {
  const { classes, cx } = useStyles();

  return (
    <a href={href} className={cx(classes.link)} data-test={dataTest}>
      <UnstyledButton
        sx={(theme) => ({
          display: "block",
          width: "100%",
          padding: theme.spacing.xs,
          borderRadius: theme.radius.sm,

          "&:hover": {
            backgroundColor: theme.colors.gray[0],
          },
        })}
      >
        <Group>
          <ThemeIcon color={color} variant="light">
            {icon}
          </ThemeIcon>

          <Text size="sm">{label}</Text>
        </Group>
      </UnstyledButton>
    </a>
  );
}

const iconSize = "1rem";
const data: MainLinkProps[] = [
  {
    icon: <IconListCheck size={iconSize} />,
    color: "blue",
    label: "Tasks",
    href: "/task",
    dataTest: "tasks-page-main-nav",
  },
  {
    icon: <IconNotebook size={iconSize} />,
    color: "teal",
    label: "Journal",
    href: "/journal",
    dataTest: "journal-page-main-nav",
  },
];

export function MainLinks() {
  const links = data.map((link) => <MainLink {...link} key={link.label} />);
  return <div>{links}</div>;
}
