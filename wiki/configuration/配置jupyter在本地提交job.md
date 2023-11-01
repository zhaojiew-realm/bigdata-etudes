
Due to following error, we need to reconfigure jupyter notebook

> Exception: Python in worker has different version 3.9 than that in driver 3.10, PySpark cannot run with different minor versions. Please check environment variables PYSPARK_PYTHON and PYSPARK_DRIVER_PYTHON are correctly set.

```shell
pip3 instll jupyter notebook
```

init jupyter
- https://dblab.xmu.edu.cn/blog/2575/

```shell
jupyter notebook --generate-config
```

change configuration
```shell
c.NotebookApp.ip='*'                     # 就是设置所有ip皆可访问  
c.NotebookApp.password = 'sha1:7c7990750e83:965c1466a4fab0849051ca5f3c5661110813795b'     # 上面复制的那个sha密文'  
c.NotebookApp.open_browser = False       # 禁止自动打开浏览器  
c.NotebookApp.port =8888                 # 端口
c.NotebookApp.notebook_dir = '/home/hadoop/jupyternotebook'  #设置Notebook启动进入的目录
```

start notebook

```shell
jupyter notebook
```