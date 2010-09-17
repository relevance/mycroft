#/bin/sh

APP_NAME="mycroft"

source bin/euca-functions.sh
create_security_group $APP_NAME "22 8080 9090" &&
provision_server_with_group $APP_NAME &&
CAP_USER=root SERVER=$EUCA_IP cap deploy:setup &&
SERVER=$EUCA_IP cap deploy &&
echo "--------------------- DONE --------------------"
echo "Mycroft deployed to $EUCA_IP:"
echo "   * curl http://$EUCA_IP:8080/ # to see it in action"
echo "   * SERVER=$EUCA_IP cap deploy # to redeploy"
echo "   * terminate_security_group_instances $APP_NAME # to terminate all running instances"
