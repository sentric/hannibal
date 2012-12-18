# -*- mode: ruby -*-
# vi: set ft=ruby :

Vagrant::Config.run do |config|

  config.vm.box_url = 'http://files.vagrantup.com/lucid32.box'
  config.vm.box = "lucid32"

  config.vm.define :hbase090 do |vm_conf|
    vm_conf.vm.host_name = "dev.hbase-0-90.hannibal"
    vm_conf.vm.customize ["modifyvm", :id, "--memory", 2048]
    vm_conf.vm.network :hostonly, "192.168.80.90"
    vm_conf.vm.provision :puppet do |puppet|
      puppet.manifests_path = "vagrant/manifests"
      puppet.manifest_file = "nodes.pp"
      puppet.module_path = "vagrant/modules"
      puppet.options = "--verbose"
    end
  end

end
