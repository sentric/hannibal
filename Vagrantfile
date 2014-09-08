# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant.configure("2") do |config|

  config.vm.define :hbase090 do |vm_conf|
    vm_conf.vm.box_url = 'http://files.vagrantup.com/lucid64.box'
    vm_conf.vm.box = "lucid64"
    vm_conf.vm.hostname = "hbase090.hannibal.dev"
    vm_conf.vm.network :private_network, ip: "192.168.80.90"
    vm_conf.vm.provision :puppet do |puppet|
      puppet.manifests_path = "vagrant/manifests"
      puppet.manifest_file = "nodes.pp"
      puppet.module_path = "vagrant/modules"
      puppet.options = "--verbose"
    end
  end

  config.vm.define :hbase092 do |vm_conf|
    vm_conf.vm.box_url = 'http://files.vagrantup.com/lucid64.box'
    vm_conf.vm.box = "lucid64"
    vm_conf.vm.hostname = "hbase092.hannibal.dev"
    vm_conf.vm.network :private_network, ip: "192.168.80.92"
    vm_conf.vm.provision :puppet do |puppet|
      puppet.manifests_path = "vagrant/manifests"
      puppet.manifest_file = "nodes.pp"
      puppet.module_path = "vagrant/modules"
      puppet.options = "--verbose"
    end
  end

  config.vm.define :hbase094 do |vm_conf|
    vm_conf.vm.box_url = 'http://files.vagrantup.com/lucid64.box'
    vm_conf.vm.box = "lucid64"
    vm_conf.vm.hostname = "hbase094.hannibal.dev"
    vm_conf.vm.network :private_network, ip: "192.168.80.94"
    vm_conf.vm.provision :puppet do |puppet|
      puppet.manifests_path = "vagrant/manifests"
      puppet.manifest_file = "nodes.pp"
      puppet.module_path = "vagrant/modules"
      puppet.options = "--verbose"
    end
  end

  config.vm.define :hbase096 do |vm_conf|
    vm_conf.vm.box = "spantree/ubuntu-precise-64"
    vm_conf.vm.hostname = "hbase096.hannibal.dev"
    vm_conf.vm.network :private_network, ip: "192.168.80.96"
    vm_conf.vm.provision :puppet do |puppet|
      puppet.manifests_path = "vagrant/manifests"
      puppet.manifest_file = "nodes.pp"
      puppet.module_path = "vagrant/modules"
      puppet.options = "--verbose"
    end
  end

  config.vm.provider :virtualbox do |vb|
      vb.customize ["modifyvm", :id, "--memory", 3072]
  end

end
