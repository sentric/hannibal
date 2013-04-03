# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant::Config.run do |config|

  config.vm.box_url = 'http://files.vagrantup.com/lucid64.box'
  config.vm.box = "lucid64"

  config.vm.define :hbase090 do |vm_conf|
    vm_conf.vm.host_name = "hbase090.hannibal.dev"
    vm_conf.vm.customize ["modifyvm", :id, "--memory", 3072]
    vm_conf.vm.network :hostonly, "192.168.80.90"
    vm_conf.vm.provision :puppet do |puppet|
      puppet.manifests_path = "vagrant/manifests"
      puppet.manifest_file = "nodes.pp"
      puppet.module_path = "vagrant/modules"
      puppet.options = "--verbose"
    end
  end

  config.vm.define :hbase092 do |vm_conf|
    vm_conf.vm.host_name = "hbase092.hannibal.dev"
    vm_conf.vm.customize ["modifyvm", :id, "--memory", 3072]
    vm_conf.vm.network :hostonly, "192.168.80.92"
    vm_conf.vm.provision :puppet do |puppet|
      puppet.manifests_path = "vagrant/manifests"
      puppet.manifest_file = "nodes.pp"
      puppet.module_path = "vagrant/modules"
      puppet.options = "--verbose"
    end
  end

  config.vm.define :hbase094 do |vm_conf|
    vm_conf.vm.host_name = "hbase094.hannibal.dev"
    vm_conf.vm.customize ["modifyvm", :id, "--memory", 3072]
    vm_conf.vm.network :hostonly, "192.168.80.94"
    vm_conf.vm.provision :puppet do |puppet|
      puppet.manifests_path = "vagrant/manifests"
      puppet.manifest_file = "nodes.pp"
      puppet.module_path = "vagrant/modules"
      puppet.options = "--verbose"
    end
  end

end
