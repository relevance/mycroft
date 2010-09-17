###########################################
# Bootstrap the Ubuntu server
###########################################

Capistrano::Configuration.instance.load do
  namespace :bootstrap do

    task :go do
      sudo "apt-get update -qq"
      sudo "apt-get install -y openjdk-6-jdk; sudo dpkg --configure -a"
      create_app_user
    end

    task :create_app_user do
      app_user = "richservices"
      sudo "sh -c '(id #{app_user}) || /usr/sbin/useradd -s /bin/bash -m #{app_user}'"
      home = "/home/#{app_user}"
      sudo "mkdir -p #{home}/.ssh"
      sudo "cp ~/.ssh/authorized_keys #{home}/.ssh/authorized_keys"
      run "sudo -H -u #{app_user} sh -c 'cd && curl --silent -OL http://github.com/technomancy/leiningen/raw/stable/bin/lein && chmod +x lein && ./lein self-install'"
      sudo "chown -R #{app_user}:#{app_user} #{home}"
      sudo "chmod 700 #{home}/.ssh"
      sudo "chmod 600 #{home}/.ssh/authorized_keys"
    end
  end
end
