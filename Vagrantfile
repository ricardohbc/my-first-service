# -*- mode: ruby -*-
# vi: set ft=ruby :

# Vagrantfile API/syntax version. Don't touch unless you know what you're doing!
VAGRANTFILE_API_VERSION = "2"

Vagrant.configure(VAGRANTFILE_API_VERSION) do |config|  
  config.vm.define "hbc-microservice-template_host" do |hbc-microservice-template_host|
    # Every Vagrant virtual environment requires a box to build off of.
    #hbc-microservice-template_host.vm.box = "chef/centos-6.6"
    
    hbc-microservice-template_host.vm.box = "hbcd/centos-6.6_1428414292_virtualbox"
    hbc-microservice-template_host.vm.box_url = "http://sd1pgo11lx.saksdirect.com:8081/artifactory/repo/centos/6.6/x86_64/1428414292/virtualbox/centos-6.6-x86_64-1428414292.virtualbox.box"

    # Create a private network, which allows host-only access to the machine
    # using a specific IP.
    hbc-microservice-template_host.vm.network "private_network", ip: "192.168.50.15"
    
    # Share an additional folder to the guest VM. The first argument is
    # the path on the host to the actual folder. The second argument is
    # the path on the guest to mount the folder. And the optional third
    # argument is a set of non-required options.
    # config.vm.synced_folder "../data", "/vagrant_data"

    # Provider-specific configuration so you can fine-tune various
    # backing providers for Vagrant. These expose provider-specific options.
    # Example for VirtualBox:
    #
   hbc-microservice-template_host.vm.provider "virtualbox" do |vb|
     #   vb.gui = true
     #
     #   # Use VBoxManage to customize the VM. For example to change memory:
     vb.memory = 4096
     vb.cpus = 2
     vb.customize ["modifyvm", :id, "--natdnshostresolver1", "on"]
     vb.customize ["modifyvm", :id, "--natdnsproxy1", "on"]
     vb.customize ["modifyvm", :id, "--nicpromisc3", "allow-all"]
   end
  
    hbc-microservice-template_host.librarian_puppet.puppetfile_dir = "puppet"
    hbc-microservice-template_host.librarian_puppet.placeholder_filename = ".gitkeep"

    # Enable provisioning with Puppet stand alone.  Puppet manifests
    # are contained in a directory path relative to this Vagrantfile.
    # You will need to create the manifests directory and a manifest in
    # the file default.pp in the manifests_path directory.
    hbc-microservice-template_host.vm.provision "puppet" do |puppet|
      puppet.manifests_path = "puppet"
      puppet.manifest_file = "site.pp"
      puppet.module_path = "puppet/modules"
    end
  end
end
