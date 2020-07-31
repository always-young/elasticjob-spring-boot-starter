### 使用步骤
#### 1.Spring启动类上加上EnableElasticJob(任务地址)
#### 2.在任务上加上@Job注解 如下
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