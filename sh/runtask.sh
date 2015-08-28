cd $NETPIPE_HOME
jobName=$0
taskName=$1
taskId=$2
javac -Djava.ext.dirs=libs -cp jobs/${jobName}.jar task.${taskName}Main $taskId 