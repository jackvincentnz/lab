# Contexts

## Overview

Here live the bounded contexts of this solution.

Ideally a bounded context should have the solution architecture:

```text
<context_name>
--> <context_api>
--> <context_implementation> // potentially many implementations to fulfill the api
```

This is so that:

1. Contexts only depend on the apis of other contexts
2. We try to align context boundaries, with subdomains, with the hosting service architectures.
