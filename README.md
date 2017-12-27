# paybreakdown

<!-- TOC depthFrom:2 depthTo:6 withLinks:1 updateOnSave:1 orderedList:0 -->

- [Problem](#problem)
	- [Assumptions:](#assumptions)
	- [Example:](#example)
- [Solution](#solution)
	- [Prerequisites](#prerequisites)
	- [Checkout](#checkout)
	- [Build](#build)
	- [Main Components](#main-components)

<!-- /TOC -->

## Problem

Write code to generate pay breakdown for a list of shifts.

The input is a list of shifts and a list of pay rates. Each shift has start (DateTime), end (DateTime) and worker id (String). Each pay rate has name (String), hourly rate (Currency), time of day start (Time) and time of day end (Time).

The result is a list of pay breakdown items. Each item has worker id (String), rate name (String), total work time (Duration), total pay (Currency).

Each rate is applicable to the time of day between start and end of that rate, e.g. a rate from 10:00 to 17:00 is applicable to work time between 10am to 5pm.

### Assumptions:

- Shifts do not overlap each other.
- Rate times do not overlap each other.
- One rate exists with no start and no end time as the default rate for work that is not covered by other rates.

### Example:

Shifts (worker, start, end)

- John, 2017-06-23 09:00, 2017-06-23 17:00
- John, 2017-06-24 06:00, 2017-06-24 14:00
- Rates (name, hourly rate, start, end)
- Default, £10.00, null, null
- Morning, £15.00, 05:00, 10:00
- Evening, £18.00, 16:30, 20:00
- Night, £20.00, 20:00, 23:00
- Pay breakdown results (worker, rate name, total work, total pay)
- John, Default, 10:30, £105.00
- John, Morning, 5:00, £75.00
- John, Evening, 0:30, £9.00



## Solution

### Prerequisites

- Java Development Kit 1.8 (either OpenJDK or Oracle)
- Apache Maven 3.3.9

### Checkout

```
git clone https://github.com/manosbatsis/paybreakdown.git
```

### Build

Build,  run tests and install in local maven repo:

```
mvn  clean install
```

### Main Components

- Models: see [src/main/java/paybreakdown/model](src/main/java/paybreakdown/model)
- App: see [src/main/java/paybreakdown/App.java](src/main/java/paybreakdown/App.java)
- Tests: see [src/test/java/paybreakdown/AppTest.java](src/test/java/paybreakdown/AppTest.java)
