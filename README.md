## The Leap assignment

### Get started

Make sure you have JDK 11 and Maven installed
- Run `mvn clean install`
- Comment out `@Disable` in [MeterFileProcessorIT](/src/test/kotlin/energy/leap/MeterFileProcessorIT.kt) and run the
  test
- You will find the generated reports in the target folder

### Assumptions

- Each meter produces one file per day
- Each meter file can only have a single reading type
- FlowDirection of 1 means supplying to the net, 0 means subtracting
- A positive price means partner receives money, a negative prices means he has to pay
- The IntervalReadings can intersect, be disjointed or joining due to whatever reasons
- The specified unit price matches the unit type
- Timestamps are supplied in UTC

### Design choices:

- Use Jackson for mapping as it is a performant library with nice features
- Use BigDecimal for currencies and durations for extra precision
- A flexible IntervalReading parser to accommodate for imperfect readings
- Use ZonedDateTime for hourly reports to account for timezones
- Round usage and currencies to 2 decimals in the report
