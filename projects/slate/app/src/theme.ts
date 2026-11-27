import { createTheme, DEFAULT_THEME } from "@mantine/core";

const inter = `"Inter Variable", ${DEFAULT_THEME.fontFamily}`;

export const theme = createTheme({
  fontFamily: inter,
  headings: { fontFamily: inter },
});
