### step
#### 1.add the EnableElasticJob(basePackage) to the main method
#### 2.add the @Job to the ElasticJob like this
```java
@Job(jobName = "myDataFlowJob")
public class MyDataFlowJob implements DataflowJob<String> {

    private static int count = 0;


    @Override
    public List<String> fetchData(ShardingContext shardingContext) {
        int maxSize = 10;
        if (count < maxSize) {
            count++;
            return Lists.newArrayList("kevin", "love", "you");
        }
        return Collections.emptyList();
    }

    @Override
    public void processData(ShardingContext shardingContext, List<String> list) {
        list.forEach(System.out::println);
    }
}
```