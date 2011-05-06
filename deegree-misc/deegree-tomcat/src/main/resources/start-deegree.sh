#!/bin/bash

if (test ! -z $JAVA_HOME) then
  $JAVA_HOME/bin/java -classpath deegree-javacheck.jar org.deegree.JavaCheck
elif (test -x $(which java)) then
  java -classpath deegree-javacheck.jar org.deegree.JavaCheck
else
  for jdir in $HOME/jdk* /usr/lib/j2* /usr/java/* /cygdrive/c/j2* /usr/local/j2* /usr/lib/jvm/java-6*
  do
    if (test -d $jdir) then
      if (test -x $jdir/bin/java) then
        export JAVA_HOME=$jdir
      fi
    fi
  done
  $JAVA_HOME/java -classpath deegree-javacheck.jar org.deegree.JavaCheck
fi

RETVAL=$?
[ $RETVAL -ne 0 ] && exit

export JAVA_OPTS=-Xmx1024M
bin/catalina.sh run &

# TODO proper browser startup after Tomcat started
sleep 5
if(test -x $(which firefox)) then
  firefox http://localhost:8080
elif(test -x $(which $BROWSER)) then
  $BROWSER http://localhost:8080
fi

# Wait forever
cat
