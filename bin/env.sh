JARS=
for i in `ls ./lib/*.jar`
do
  JARS=${JARS}:${i}
done

export JARS
export SRC_DIR=src
export TEST_DIRS=test
export JVM_ARGS="-Xmx1G"
