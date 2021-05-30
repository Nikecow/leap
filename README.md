## The Leap assignment

### Get started

- Run `mvn clean install`
- Comment out `@Disable` in [MeterFileProcessorIT](/src/test/kotlin/energy/leap/MeterFileProcessorIT.kt) and run the
  test to parse all files in the resource folder.
- You will find the reports in the target folder

### Assumptions

- Each meter produces one file per day
- Each meter file can only have a single reading type
- FlowDirection of 1 means supplying to the net, 0 means subtracting
- A positive price means partner receives money, a negative prices means he has to pay
- The IntervalReadings can intersect, be disjointed or joining due to whatever reasons
- The specified unit price matches the unit type
- Timestamps are supplied in UTC

### Design choices:

- Use BigDecimal for prices for extra precision, as small differences can add up
- A flexible IntervalReading parser to accommodate for imperfect readings
- Use ZonedDateTime for hourly reports to account for timezones
- Round currencies to 2 decimals in the report
