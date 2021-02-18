# Standalone Stream Sample

## Building the apps

Use the appropriate binder profiles `kafka` (active by default) or `rabbit` to build a binary for use with that binder.

```bash
$./mvnw clean package -Pkafka
```
or

```bash
$./mvnw clean package -Pkafka
```

## Building Docker Images

```bash
$./mvnw spring-boot:build-image
```

## Building the distribution zip file

```bash
$./mvnw package -Pdist

```

This must be run from this directory and will build `dist/usage-cost-stream-sample.zip` 