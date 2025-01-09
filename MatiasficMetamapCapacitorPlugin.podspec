require 'json'

package = JSON.parse(File.read(File.join(__dir__, 'package.json')))

Pod::Spec.new do |s|
  s.name = 'MatiasficMetamapCapacitorPlugin'
  s.version = package['version']
  s.summary = package['description']
  s.license = package['license']
  s.homepage = package['repository']['url']
  s.author = package['author']
  s.source = { :path => '.' } # Local path for the plugin's source
  s.source_files = 'ios/Plugin/**/*.{swift,h,m,c,cc,mm,cpp}' # Source files for Capacitor plugin
  s.ios.deployment_target  = '13.0'
  s.dependency 'Capacitor'
  s.dependency 'MetaMapSDK', '3.22.4'
  s.static_framework = true # Use static framework to support dependencies like Incode SDK
  s.swift_version = '5.1'
end
