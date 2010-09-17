$LOAD_PATH << "config"

set :application, "rich-services"
set :scm, :git
set(:current_branch)     { `git branch`.match(/\* (\S+)\s/m)[1] || raise("Couldn't determine current branch") }
set :branch,             defer { current_branch }
set :repository, "git@github.com:relevance/rich-services.git"
set :deploy_to, "/var/www/apps/#{application}"

set :deploy_via, :copy
set :copy_exclude, [".git/*"]
set :copy_compression, :bz2 # Also valid are :zip and :bz2    
set :use_sudo, !!ENV["SUDO"]

set :domain, ENV['SERVER'] || abort("Error - You must specify a server to deploy to as an environment variable - for example: 'SERVER=web01.example.com cap deploy'")

set :non_privileged_user, "richservices"
set :user, ENV["CAP_USER"] || non_privileged_user
ssh_options[:auth_methods] = ["publickey"]
default_run_options[:pty] = true

role :app, domain
role :web, domain
role :db,  domain, :primary => true

require 'bootstrap'

before "deploy:setup",       "bootstrap:go"
after  "deploy:setup",       "deploy:fix_setup_permissions"
after  "deploy:update_code", "deploy:create_shared_dirs"
after  "deploy:update_code", "deploy:symlink_lib"
after  "deploy",             "deploy:cleanup"

namespace :deploy do
  
  task :create_shared_dirs do
    run "mkdir -p #{shared_path}/lib"
    run "mkdir -p #{shared_path}/log"
  end
  
  task :symlink_lib do
    run "ln -fs #{shared_path}/lib #{latest_release}/"
  end
  
  task :fix_setup_permissions do
    sudo "chown -R #{non_privileged_user}:#{non_privileged_user} #{deploy_to}"
  end
  
  task :start do
    run "cd #{current_path} && ~/lein deps"
    run "cd #{current_path} && nohup bin/daemon-start.sh"
  end

  task :restart do
    stop
    start
  end
 
  task :stop do
    run "cd #{current_path} && bin/daemon-stop.sh"
  end
  
  task :cold do
    update
    start
  end
end
