# JSON Persistence

The JSON Persistence Service is based on simple key-value store that only saves the last value. The intention is to use this for `restoreOnStartup` items because all other persistence options have their drawbacks if values are only needed for reload.  They:

* grow in time
* require complex installs (`mysql`, `influxdb`, ...)
* `rrd4j` can't store all item types (only numeric types)

Querying the json persistence service for historic values other than the last value make no sense since the persistence service can only store one value per item.

## Configuration

TODO

## Troubleshooting

Restore of items after startup is taking some time. Rules are already started in parallel. Especially in rules that are started via `System started` trigger, it may happen that the restore is not completed resulting in undefined items. In these cases the use of restored items has to be delayed by a couple of seconds. This delay has to be determined experimentally.
