import { createStyles, Container, Tabs, rem, Title } from "@mantine/core";
import Tasks from "../tasks";

const useStyles = createStyles((theme) => ({
  header: {
    paddingTop: theme.spacing.xs,
    backgroundColor: theme.colors.gray[0],
    borderBottom: `${rem(1)} solid ${theme.colors.gray[2]}`,
    marginBottom: theme.spacing.xs,
  },

  tabsList: {
    borderBottom: "0 !important",
  },

  tab: {
    fontWeight: 500,
    height: rem(38),
    backgroundColor: "transparent",

    "&:hover": {
      backgroundColor:
        theme.colorScheme === "dark"
          ? theme.colors.dark[5]
          : theme.colors.gray[1],
    },

    "&[data-active]": {
      backgroundColor: theme.white,
      borderColor: theme.colors.gray[2],
    },
  },
}));

export default function App() {
  const { classes } = useStyles();

  const tabs = ["Home"];
  const items = tabs.map((tab) => (
    <Tabs.Tab value={tab} key={tab}>
      {tab}
    </Tabs.Tab>
  ));

  return (
    <>
      <div className={classes.header}>
        <Container>
          <Title order={1}>Tasklist</Title>
        </Container>
        <Container mt="xs">
          <Tabs
            defaultValue={tabs[0]}
            variant="outline"
            classNames={{
              tabsList: classes.tabsList,
              tab: classes.tab,
            }}
          >
            <Tabs.List>{items}</Tabs.List>
          </Tabs>
        </Container>
      </div>

      <Container>
        <Tasks />
      </Container>
    </>
  );
}
