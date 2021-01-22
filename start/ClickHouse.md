## ClickHouse

使用Docker启动 clickhouse server

[官方地址](https://hub.docker.com/r/yandex/clickhouse-server/)

docker run -d --name clickhouse --ulimit nofile=262144:262144 -p 8123:8123 -p 9009:9009 -p 9000:9000 --volume=/home/clickhouse:/var/lib/clickhouse yandex/clickhouse-server 

  

挂配置

```
-v /path/to/your/config.xml:/etc/clickhouse-server/config.xml
```

安装clickhouse client

```
sudo yum -y install yum-utils
sudo rpm --import https://repo.clickhouse.tech/CLICKHOUSE-KEY.GPG
sudo yum-config-manager --add-repo https://repo.clickhouse.tech/rpm/stable/x86_64
sudo yum -y install clickhouse-client
```

使用client

clickhouse-client



vim /home/metrika.xml

启动zookeeper

docker run -p 2181:2181 --name zookeeper --restart always -d zookeeper

集群配置

docker run -d --name clickhouse --ulimit nofile=262144:262144 -p 8123:8123 -p 9009:9009 -p 9000:9000 --volume=/home/clickhouse:/var/lib/clickhouse  -v /home/config.xml:/etc/clickhouse-server/config.xml -v /home/metrika.xml:/etc/clickhouse-server/metrika.xml yandex/clickhouse-server 

config.xml配置

```
<yandex>

<logger>
        <level>trace</level>
        <log>/usr/local/clickhouse/log/server.log</log>
        <errorlog>/usr/local/clickhouse/log/error.log</errorlog>
        <size>1000M</size>
        <count>10</count>
    </logger>
    
    <http_port>8123</http_port>
    <tcp_port>9000</tcp_port>
    <interserver_http_port>9009</interserver_http_port>
    <listen_host>0.0.0.0</listen_host>
 
    <path>/usr/local/clickhouse/data/clickhouse/</path>
    <tmp_path>/usr/local/clickhouse/data/clickhouse/tmp/</tmp_path>
    <users_config>users.xml</users_config>
 
    <default_profile>default</default_profile>
    <default_database>default</default_database>
    <remote_servers incl="clickhouse_remote_servers" />
 
    <zookeeper incl="zookeeper-servers" optional="true" />
    <macros incl="macros" optional="true" />
    <include_from>/etc/clickhouse-server/metrika.xml</include_from>
 
    <mark_cache_size>5368709120</mark_cache_size>
 
</yandex>
```

metrika.xml配置

```
<yandex>
 
<clickhouse_remote_servers>
    <report_shards_replicas>
        <shard>
            <weight>1</weight>
            <internal_replication>false</internal_replication>
            <replica>
                <host>172.26.22.65</host>
                <port>9000</port>
            </replica>
            <replica>
                <host>172.26.22.64</host>
                <port>9000</port>
            </replica>
        </shard>
        <shard>
            <weight>1</weight>
            <internal_replication>false</internal_replication>
            <replica>
                <host>172.26.22.64</host>
                <port>9000</port>
            </replica>
            <replica>
                <host>172.26.22.63</host>
                <port>9000</port>
            </replica>
        </shard>
	<shard>
            <weight>1</weight>
            <internal_replication>false</internal_replication>
            <replica>
                <host>172.26.22.63</host>
                <port>9000</port>
            </replica>
            <replica>
                <host>172.26.22.65</host>
                <port>9000</port>
            </replica>
        </shard>
    </report_shards_replicas>
</clickhouse_remote_servers>
 
<macros>
    <replica>172.26.22.65</replica>
</macros>
 
<networks>
   <ip>::/0</ip>
</networks>
 
<zookeeper-servers>
    <node index="1">
        <host>172.26.22.65</host>
        <port>2181</port>
    </node>
</zookeeper-servers>
 
<clickhouse_compression>
    <case>
        <min_part_size>10000000000</min_part_size>
        <min_part_size_ratio>0.01</min_part_size_ratio>
        <method>lz4</method>
    </case>
</clickhouse_compression>
</yandex>
```

创建本地表，分布式表

CREATE TABLE ck_local (UnixDate Date,Year UInt16) ENGINE = MergeTree(UnixDate, (Year, UnixDate), 8192); 

CREATE TABLE ck_all AS ck_local ENGINE = Distributed(report_shards_replicas, default, ck_local, rand());

查看集群

select * from system.clusters; 

插入数据

insert into ck_all (UnixDate,Year)values('2010-03-20',2010); 



JDBC

```
<dependency>
    <groupId>ru.yandex.clickhouse</groupId>
    <artifactId>clickhouse-jdbc</artifactId>
    <version>0.2.4</version>
</dependency>
```



从MySQL导入整库

```
CREATE DATABASE mysql_db ENGINE = MySQL('localhost:3306', 'test', 'my_user', 'user_password')
```

导入表

CREATE TABLE stock_daily ENGINE = MergeTree ORDER BY id AS SELECT * FROM mysql(' localhost:3306','database','table','root','pwd!');



JDBC
使用方法基本没有什么区别

```
@Test
public void testSayHello() throws SQLException {
    String sqlDB = "show databases";//查询数据库
    String sqlTab = "show tables";//查看表
    //String sqlCount = "select stock_no,count(*) from stock_daily where view_date > '2012-01-01' and view_date < '2013-01-01' group by stock_no order by count(stock_no) desc";
    String sqlCount = "select * from stock_daily where view_date > '2012-01-01' and view_date < '2013-01-01' limit 10000,10";
    String sqlInsert = "insert into ck_local (UnixDate,Year)values('2010-03-21',2011);";
    exeSql(sqlDB);
    exeSql(sqlTab);
    exeSql(sqlInsert);
    exeSql(sqlCount);
}

public void exeSql(String sql){
    String address = "jdbc:clickhouse://39.99.174.255:8123/default";
    Connection connection = null;
    Statement statement = null;
    ResultSet results = null;
    try {
        Class.forName("ru.yandex.clickhouse.ClickHouseDriver");
        connection = DriverManager.getConnection(address);
        statement = connection.createStatement();
        long begin = System.currentTimeMillis();
        results = statement.executeQuery(sql);
        long end = System.currentTimeMillis();
        System.out.println("执行（"+sql+"）耗时："+(end-begin)+"ms");
        ResultSetMetaData rsmd = results.getMetaData();
        List<Map> list = new ArrayList();
        while(results.next()){
            Map map = new HashMap();
            for(int i = 1;i<=rsmd.getColumnCount();i++){
                map.put(rsmd.getColumnName(i),results.getString(rsmd.getColumnName(i)));
            }
            list.add(map);
        }
        for(Map map : list){
            System.err.println(map);
        }
    } catch (Exception e) {
        e.printStackTrace();
    }finally {//关闭连接
        try {
            if(results!=null){
                results.close();
            }
            if(statement!=null){
                statement.close();
            }
            if(connection!=null){
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
```