function install_euca_tools {
  pushd ~/tmp
	curl -L -o euca_deps.tgz http://open.eucalyptus.com/sites/all/modules/pubdlcnt/pubdlcnt.php?file=http://eucalyptussoftware.com/downloads/releases/euca2ools-1.2-src-deps.tar.gz &&
	tar xzf euca_deps.tgz && cd euca2ools-1.2-src-deps/ &&
	tar xzf M2Crypto-0.19.1.tar.gz && cd M2Crypto-0.19.1 && sudo python setup.py install && cd .. &&
	tar xzf boto-1.8d.tar.gz && cd boto-1.8d && sudo python setup.py install && cd ..
	cd .. &&
	curl -L -o euca_tools.tgz http://open.eucalyptus.com/sites/all/modules/pubdlcnt/pubdlcnt.php?file=http://eucalyptussoftware.com/downloads/releases/euca2ools-1.2.tar.gz &&
	tar xzf euca_tools.tgz &&
	cd euca2ools-1.2 &&
	sudo make # fails!
	sudo cp bin/euca-* /usr/local/bin/ &&
	cd ..
	popd
}

function validate_environment_variables {
  if [ -n "$EC2_URL" -a -n "$EC2_ACCESS_KEY" -a -n "$EC2_SECRET_KEY" ]; then
    return 0
  else
    echo "You must set EC2_SECRET_KEY, EC2_ACCESS_KEY, and EC2_URL"
    echo "If you're using EC2, export EC2_URL=https://ec2.amazonaws.com:443"
    echo "Get the EC2_ACCESS_KEY and EC2_SECRET_KEY from https://aws-portal.amazon.com/gp/aws/developer/account/index.html?ie=UTF8&action=access-key"
    exit 1
  fi
}

function create_security_group {
  local group=$1
  local ports=$2

  euca-delete-group $group
  euca-add-group -d "$group" $group
  
  for port in ${ports[@]} ; do
    euca-authorize -P tcp -p $port -s 0.0.0.0/0 $group
  done
}

function start_instance {
  local group=$1
  export EUCA_IDENTIFIER=`euca-run-instances -k $group ami-19a34270 -g $group | grep INSTANCE | awk '{print $2}'`
  export EUCA_IP="pending"
}

function extract_ip {
  local group=$1
  echo "Waiting for $EUCA_IDENTIFIER (provisioned for $1) to start"
  EUCA_IP=`euca-describe-instances $EUCA_IDENTIFIER | grep INSTANCE | awk '{print $4}'`
  while [ "$EUCA_IP" = "pending" ]
  do
    sleep 5
    EUCA_IP=`euca-describe-instances $EUCA_IDENTIFIER | grep INSTANCE | awk '{print $4}'`
  done
  export EUCA_IP
}

function ensure_ssh {
  local host=$1
  local ssh_status=1
  echo "Waiting for ssh connection on $host"
  `ssh -q -q -o "BatchMode=yes" -o "ConnectTimeout 5" -o "StrictHostKeyChecking no" root@$host "echo 2>&1" && return 0 || return 1`
  ssh_status=$?
  while [ $ssh_status -ne 0 ]
  do
    sleep 5
    `ssh -q -q -o "BatchMode=yes" -o "ConnectTimeout 5" -o "StrictHostKeyChecking no" root@$host "echo 2>&1" && return 0 || return 1`
    ssh_status=$?
  done
}

function servers_running_as_security_group {
  local group=$1
  export EUCA_SERVERS="`euca-describe-instances | grep -E \"$group(\s|$)\" -A 1 | grep INSTANCE | grep -v terminated | grep -v shutting-down | awk '{print $4}'`"
}

function instances_running_as_security_group {
  local group=$1
  export EUCA_INSTANCES="`euca-describe-instances | grep -E \"$group(\s|$)\" -A 1 | grep INSTANCE | grep -v terminated | grep -v shutting-down | awk '{print $2}'`"
}

function terminate_security_group_instances {
  local group=$1
  instances_running_as_security_group $group
  euca-terminate-instances $EUCA_INSTANCES
}

function provision_server_with_group {
  start_instance $1
  extract_ip $1
  ensure_ssh $EUCA_IP
}

function confirm_not_in_git_repo {
  if [ -d .git/ ]; then
	echo "Error - you are inside a git repo, instead of being in the parent of your Verizon projects -- please cd to the correct directory first!"
	exit 1
fi
}
