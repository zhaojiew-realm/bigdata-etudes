启动Ubuntu实例

```shell
#ubuntu2004
image_id=ami-0ee5d3b4bc88442f4

# 启动在公有子网
aws ec2 run-instances --image-id $image_id \
	--instance-type t3.medium \
	--key-name temp-key \
	--count 1 \
    --subnet-id subnet-027025e9d9760acdd \
    --security-group-ids sg-096df1a0cb9a6d7e9 \
    --tag-specifications 'ResourceType=instance,Tags=[{Key=Name,Value=test-airflow}]' 'ResourceType=volume,Tags=[{Key=Name,Value=can_delete}]'
```

配置python3

> Successful installation requires a Python 3 environment. Starting with Airflow 2.3.0, Airflow is tested with Python 3.7, 3.8, 3.9, 3.10. Note that Python 3.11 is not yet supported.

```shell
sudo apt update
sudo apt install python3-pip -y
pip3 install virtualenv
sudo ln -s /home/ubuntu/.local/bin/virtualenv /usr/local/bin/virtualenv 

mkdir airflow && cd airflow
virtualenv venv
source venv/bin/activate
```

安装airflow

```shell
export AIRFLOW_HOME=~/airflow
AIRFLOW_VERSION=2.6.2
PYTHON_VERSION="$(python3 --version | cut -d " " -f 2 | cut -d "." -f 1-2)"
CONSTRAINT_URL="https://raw.githubusercontent.com/apache/airflow/constraints-${AIRFLOW_VERSION}/constraints-${PYTHON_VERSION}.txt"
pip3 install "apache-airflow==${AIRFLOW_VERSION}" --constraint "${CONSTRAINT_URL}"
pip3 install mysql-connector-python
```

使用standalone模式启动

- 将创建 `$AIRFLOW_HOME`文件夹，并创建默认的`AIRFLOW. cfg`文件\
- 使用 SQLite 数据库和`SequentialExecator` ，只能按顺序执行任务

```shell
airflow standalone
```

手动启动

```shell
airflow db init

airflow users create \
    --username admin \
    --firstname Peter \
    --lastname Parker \
    --role Admin \
    --email spiderman@superhero.org
Password:
Repeat for confirmation:
[2023-06-21T14:01:34.285+0000] {manager.py:212} INFO - Added user admin
User "admin" created with role "Admin"

airflow webserver --port 8099
airflow scheduler
```

## 修改airflow配置

修改调度器

```shell
vim ~/.airflow/airflow

# The executor class that airflow should use. Choices include
# ``SequentialExecutor``, ``LocalExecutor``, ``CeleryExecutor``, ``DaskExecutor``,
# ``KubernetesExecutor``, ``CeleryKubernetesExecutor`` or the
# full import path to the class when using a custom executor.
executor = LocalExecutor
```

使用mysql数据库

> https://airflow.apache.org/docs/apache-airflow/stable/howto/set-up-database.html#setting-up-a-mysql-database

```
sudo apt install mariadb-server
mysql_secure_installation
```

创建用户

```sql
CREATE USER 'admin'@'%' IDENTIFIED BY 'passwd';
GRANT ALL PRIVILEGES ON *.* TO 'admin'@'%' WITH GRANT OPTION;
FLUSH PRIVILEGES;

# 或者
CREATE DATABASE airflow_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
CREATE USER 'airflow_user' IDENTIFIED BY 'airflow_pass';
GRANT ALL PRIVILEGES ON airflow_db.* TO 'airflow_user';
```

配置驱动

```
pip3 install mysql-connector-python
```

连接配置

```shell
[database]
# The SqlAlchemy connection string to the metadata database.
# SqlAlchemy supports many different database engines.
# More information here:
# http://airflow.apache.org/docs/apache-airflow/stable/howto/set-up-database.html#database-uri
#sql_alchemy_conn = sqlite:////home/ubuntu/airflow/airflow.db
sql_alchemy_conn = mysql+mysqlconnector://admin:passwd@localhost:3306/airflow_db
```

初始化

```
airflow db init
```

启动airflow脚本

```bash
#!/bin/bash
case $1 in
"start"){
	echo "starting airflow"
	airflow webserver -p 8099 -D;airflow scheduler -D
};;
"stop"){
	echo "stoping airflow"
	ps -ef|egrep 'scheduler|airflow-webserver'| grep -v grep|awk '{print $2}'|xargs kill -15
};;
esac
```

示例dags的位置

```
~/airflow/venv/lib/python3.8/site-packages/airflow/example_dags$ ls
__init__.py                              example_dynamic_task_mapping.py                             example_sensor_decorator.py         example_trigger_controller_dag.py
__pycache__                              example_dynamic_task_mapping_with_no_taskflow_operators.py  example_sensors.py                  example_trigger_target_dag.py
example_bash_operator.py                 example_external_task_marker_dag.py                         example_setup_teardown.py           example_xcom.py
example_branch_datetime_operator.py      example_kubernetes_executor.py                              example_setup_teardown_taskflow.py  example_xcomargs.py
example_branch_day_of_week_operator.py   example_latest_only.py                                      example_short_circuit_decorator.py  libs
example_branch_labels.py                 example_latest_only_with_trigger.py                         example_short_circuit_operator.py   plugins
example_branch_operator.py               example_local_kubernetes_executor.py                        example_skip_dag.py                 sql
example_branch_operator_decorator.py     example_nested_branch_dag.py                                example_sla_dag.py                  subdags
example_branch_python_dop_operator_3.py  example_params_trigger_ui.py                                example_subdag_operator.py          tutorial.py
example_complex.py                       example_params_ui_tutorial.py                               example_task_group.py               tutorial_dag.py
example_dag_decorator.py                 example_passing_params_via_test_command.py                  example_task_group_decorator.py     tutorial_taskflow_api.py
example_datasets.py                      example_python_operator.py                                  example_time_delta_sensor_async.py  tutorial_taskflow_api_virtualenv.py
```

## 集成aws服务认证

认证方面仍旧使用boto3，或者创建连接指定凭证

> https://airflow.apache.org/docs/apache-airflow-providers-amazon/stable/connections/aws.html

默认aws连接自动从.aws目录下获取凭证

![](https://s2.loli.net/2023/06/21/5TajUhiBORbxyJD.png)

配置ec2默认凭证后，在dags中使用awscli获取当前身份

```
[2023-06-21, 14:59:36 UTC] {subprocess.py:75} INFO - Running command: ['/usr/bin/bash', '-c', 'aws sts get-caller-identity']
[2023-06-21, 14:59:36 UTC] {subprocess.py:86} INFO - Output:
[2023-06-21, 14:59:36 UTC] {subprocess.py:93} INFO - {
[2023-06-21, 14:59:36 UTC] {subprocess.py:93} INFO -     "UserId": "AROAQRIBWRJKKIG7SFQ6E:i-0a7206f221448090d",
[2023-06-21, 14:59:36 UTC] {subprocess.py:93} INFO -     "Account": "xxxxxxx",
[2023-06-21, 14:59:36 UTC] {subprocess.py:93} INFO -     "Arn": "arn:aws-cn:sts::xxxxxxx:assumed-role/MyEc2AdministratorAccess/i-0a7206xxxxx0d"
[2023-06-21, 14:59:36 UTC] {subprocess.py:93} INFO - }
[2023-06-21, 14:59:36 UTC] {subprocess.py:97} INFO - Command exited with return code 0
```

或者使用脚本获取临时凭证

```shell
function switchrole(){
    local aws_credential=$(aws sts assume-role \
        --duration-seconds 900 \
        --role-arn arn:aws-cn:iam::037047667284:role/MyTestRole \
        --role-session-name MyTestSession \
        --profile default \
        --output json  | jq -r '.Credentials')
    if [[ -z $aws_credential ]]; then
        echo "Failed to assume role, please ensure that your account is valid or the role have appropriate permissions."
        exit 254
    fi
    echo "export AWS_ACCESS_KEY_ID=$(echo $aws_credential | jq -r '.AccessKeyId')"
    echo "export AWS_SECRET_ACCESS_KEY=$(echo $aws_credential | jq -r '.SecretAccessKey')"
    echo "export AWS_SESSION_TOKEN=$(echo $aws_credential | jq -r '.SessionToken')"
    echo "export AWS_DEFAULT_REGION=cn-north-1"

    echo "os.environ['AWS_ACCESS_KEY_ID'] = '$(echo $aws_credential | jq -r '.AccessKeyId')'"
    echo "os.environ['AWS_SECRET_ACCESS_KEY'] = '$(echo $aws_credential | jq -r '.SecretAccessKey')'"
    echo "os.environ['AWS_SESSION_TOKEN'] = '$(echo $aws_credential | jq -r '.SessionToken')'"
    echo "os.environ['AWS_DEFAULT_REGION'] = 'cn-north-1'"
}
switchrole
```

编写dags设置connection链接

> https://airflow.apache.org/docs/apache-airflow/stable/howto/connection.html
>
> https://airflow.apache.org/docs/apache-airflow-providers-amazon/stable/connections/aws.html#using-instance-profile

Connections may be defined in the following ways:

> in [environment variables](https://airflow.apache.org/docs/apache-airflow/stable/howto/connection.html#environment-variables-connections)
>
> in an external [Secrets Backend](https://airflow.apache.org/docs/apache-airflow/stable/administration-and-deployment/security/secrets/secrets-backend/index.html)
>
> in the [Airflow metadata database](https://airflow.apache.org/docs/apache-airflow/stable/howto/connection.html#connections-in-database) (using the [CLI](https://airflow.apache.org/docs/apache-airflow/stable/howto/connection.html#connection-cli) or [web UI](https://airflow.apache.org/docs/apache-airflow/stable/howto/connection.html#creating-connection-ui))

```py
from airflow.models.connection import Connection

conn = Connection(
    conn_id="sample_aws_connection",
    conn_type="aws",
    login="ASIxxxxxxx6OF2",  # Reference to AWS Access Key ID
    password="1omZoPxxxxxxxxx/E0qoUc",  # Reference to AWS Secret Access Key
    extra={
  "aws_session_token": "FwoDYXdzEJf//////////wEaDBWTGxxxxxYH/7mCvZCjatQxSioosykBjItNGO0VhcpT18EZFPIR0/Ovfj8eD1G5LJinDjTqgb/4AJza5ZRdr5fN9oQNtKd",
  "region_name": "cn-north-1"
	}
)

output:
[2023-06-21T15:15:56.117+0000] {crypto.py:83} WARNING - empty cryptography key - values will not be stored encrypted.
AIRFLOW_CONN_SAMPLE_AWS_CONNECTION=aws://ASIAxxxxxxxxxxxZ6OF2:1omZoxxxxxxxxxxxxxxxxmS%2FE0qoUc@/?aws_session_token=FwoDYXdzEJxTqgb%2F4AJza5ZRdr5fN9oQNtKd&region_name=cn-north-1
(False, 'Unknown hook type "aws"')
```

airflow cli添加链接配置

```
airflow connections add aws_conn --conn-uri aws://ASIAxxxxxxxxxxxZ6OF2:1omZoxxxxxxxxxxxxxxxxmS%2FE0qoUc@/?aws_session_token=FwoDYXdzEJfx%2F4AJza5ZRdr5fN9oQNtKd&region_name=cn-north-1
```

在dags中指定这个环境变量，但是貌似没啥反应

```
os.environ['AIRFLOW_CONN_AWS_DEFAULT']=“aws://ASIAQRIBWRJKE3DZ6OF2:1omZoP6MiAev5Xgt5%2Fsi0OTyPVmCClcmS%2FE0qoUc@/?aws_session_token=FwoDYXdzxD1G5LJinDjTqgb%2F4AJza5ZRdr5fN9oQNtKd”
```

## 编写dags启动ec2实例

> https://github.com/apache/airflow/tree/providers-amazon/8.1.0/tests/system/providers/amazon/aws

官方示例如下

```py
from __future__ import annotations

from datetime import datetime
from operator import itemgetter

import boto3

from airflow import DAG
from airflow.decorators import task
from airflow.models.baseoperator import chain
from airflow.providers.amazon.aws.operators.ec2 import (
    EC2CreateInstanceOperator,
    EC2StartInstanceOperator,
    EC2StopInstanceOperator,
    EC2TerminateInstanceOperator,
)
from airflow.providers.amazon.aws.sensors.ec2 import EC2InstanceStateSensor
from airflow.utils.trigger_rule import TriggerRule

DAG_ID = "aaa_test_ec2"


@task
def get_latest_ami_id():
    """Returns the AMI ID of the most recently-created Amazon Linux image"""

    image_prefix = "Amazon Linux*"

    images = boto3.client("ec2").describe_images(
        Filters=[
            {"Name": "description", "Values": [image_prefix]},
            {"Name": "architecture", "Values": ["arm64"]},
        ],
        Owners=["amazon"],
    )
    # Sort on CreationDate
    sorted_images = sorted(images["Images"], key=itemgetter("CreationDate"), reverse=True)
    return sorted_images[0]["ImageId"]



@task
def create_key_pair(key_name: str):
    client = boto3.client("ec2")

    key_pair_id = client.create_key_pair(KeyName=key_name)["KeyName"]
    # Creating the key takes a very short but measurable time, preventing race condition:
    client.get_waiter("key_pair_exists").wait(KeyNames=[key_pair_id])

    return key_pair_id

@task(trigger_rule=TriggerRule.ALL_DONE)
def delete_key_pair(key_pair_id: str):
    boto3.client("ec2").delete_key_pair(KeyName=key_pair_id)

@task
def parse_response(instance_ids: list):
    return instance_ids[0]

with DAG(
    dag_id=DAG_ID,
    schedule="@once",
    start_date=datetime(2021, 1, 1),
    tags=["example"],
    catchup=False,
) as dag:
    env_id = "testec2"
    instance_name = f"{env_id}-instance"
    key_name = create_key_pair(key_name=f"{env_id}_key_pair")
    image_id = get_latest_ami_id()

    config = {
        "InstanceType": "t4g.micro",
        "KeyName": key_name,
        "TagSpecifications": [
            {"ResourceType": "instance", "Tags": [{"Key": "Name", "Value": instance_name}]}
        ],
        "MetadataOptions": {"HttpEndpoint": "enabled", "HttpTokens": "required"},
    }

    create_instance = EC2CreateInstanceOperator(
        task_id="create_instance",
        image_id=image_id,
        max_count=1,
        min_count=1,
        config=config,
    )
    # [END howto_operator_ec2_create_instance]
    create_instance.wait_for_completion = True
    instance_id = parse_response(create_instance.output)
    # [START howto_operator_ec2_stop_instance]
    stop_instance = EC2StopInstanceOperator(
        task_id="stop_instance",
        instance_id=instance_id,
    )
    # [END howto_operator_ec2_stop_instance]
    stop_instance.trigger_rule = TriggerRule.ALL_DONE

    # [START howto_operator_ec2_start_instance]
    start_instance = EC2StartInstanceOperator(
        task_id="start_instance",
        instance_id=instance_id,
    )
    # [END howto_operator_ec2_start_instance]

    # [START howto_sensor_ec2_instance_state]
    await_instance = EC2InstanceStateSensor(
        task_id="await_instance",
        instance_id=instance_id,
        target_state="running",
    )
    # [END howto_sensor_ec2_instance_state]

    # [START howto_operator_ec2_terminate_instance]
    terminate_instance = EC2TerminateInstanceOperator(
        task_id="terminate_instance",
        instance_ids=instance_id,
        wait_for_completion=True,
    )
    # [END howto_operator_ec2_terminate_instance]
    terminate_instance.trigger_rule = TriggerRule.ALL_DONE
    chain(
        # TEST SETUP
        key_name,
        image_id,
        # TEST BODY
        create_instance,
        instance_id,
        stop_instance,
        start_instance,
        await_instance,
        terminate_instance,
        # TEST TEARDOWN
        delete_key_pair(key_name),
    )
```

最终的运行效果

![](https://s2.loli.net/2023/06/22/vZMuw6YsSpliXqh.png)

状态转移

![](https://s2.loli.net/2023/06/22/QWKA7IHJNMO2g3f.png)

