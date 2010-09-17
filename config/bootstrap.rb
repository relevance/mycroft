###########################################
# Bootstrap the Ubuntu server
###########################################

Capistrano::Configuration.instance.load do
  namespace :bootstrap do

    task :go do
      sudo "apt-get update -qq"
      sudo "apt-get install -y openjdk-6-jdk; sudo dpkg --configure -a"
      create_app_user
      reverse_proxy
    end

    task :create_app_user do
      sudo "sh -c '(id #{non_privileged_user}) || /usr/sbin/useradd -s /bin/bash -m #{non_privileged_user}'"
      home = "/home/#{non_privileged_user}"
      sudo "mkdir -p #{home}/.ssh"
      sudo "cp ~/.ssh/authorized_keys #{home}/.ssh/authorized_keys"
      run "sudo -H -u #{non_privileged_user} sh -c 'cd && curl --silent -OL http://github.com/technomancy/leiningen/raw/stable/bin/lein && chmod +x lein && ./lein self-install'"
      sudo "chown -R #{non_privileged_user}:#{non_privileged_user} #{home}"
      sudo "chmod 700 #{home}/.ssh"
      sudo "chmod 600 #{home}/.ssh/authorized_keys"
    end
    
    task :reverse_proxy do
      sudo 'apt-get install nginx'
      upload "config/mime.types", "/etc/nginx/mime.types"
      upload "config/reverse_proxy_config.nginx", "/etc/nginx/nginx.conf"
      sudo '/etc/init.d/nginx restart'
    end
  end
end
