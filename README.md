```shell
$ ./gradlew compareKotlinCompile
```

## What I found out

```
info: kotlinc-jvm 1.6.10 (JRE 11.0.12+8-LTS-237)
```

- Specifying the type of variable does not change the compilation time.
  - `val a = 1`
  - `val a: Int = 1`
- Explicitly specifying type parameters for generic functions slightly speeds up the compilation time. However, since unnecessary type parameters are specified, a warning will appear.
  - `val a = listOf(1, 2).map { it * 2.0 }.reduce(Double::plus)`
  - `val a = listOf<Int>(1, 2).map<Int, Double> { it * 2.0 }.reduce<Double, Double>(Double::plus)`
