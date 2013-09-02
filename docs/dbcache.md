
# Database Cache

If you are using the database backed cache, you will need to create the
following schema as matches your setup.

## MySQL

```sql
CREATE TABLE `groxy_cache` (
  `id` varchar(255) NOT NULL,
  `data` longtext NOT NULL,
  PRIMARY KEY (`id`)
);
```

