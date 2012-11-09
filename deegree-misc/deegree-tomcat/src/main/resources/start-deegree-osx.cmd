#!/bin/sh

setJavaHome() {
  for jdir in /usr/lib/jvm/java-7* /usr/lib/jvm/java-6* $HOME/jdk* /usr/lib/j2* /usr/java/* /cygdrive/c/j2* /usr/local/j2* 
  do    
    if (test -z $JAVA_HOME && test -d $jdir) then
      if (test -x $jdir/bin/java) then
        export JAVA_HOME=$jdir
      fi
    fi
  done
  if (test -z $JAVA_HOME) then
    echo "JAVA_HOME not set and automatic detection failed. Is a suitable JDK installed?"
    exit
  fi
}

if (test -z $JAVA_HOME) then
  setJavaHome  
fi
$JAVA_HOME/bin/java -classpath deegree-javacheck.jar org.deegree.JavaCheck
RETVAL=$?
[ $RETVAL -ne 0 ] && exit

export JAVA_OPTS=-Xmx1024M
bin/catalina.sh run
