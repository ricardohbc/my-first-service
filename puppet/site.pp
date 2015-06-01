include 'sbtubuntu'

class { 'java':
  package => 'openjdk-7-jdk'
}

class { 'nodejs':
  version => 'latest'
}

include '::mongodb::server'

class {'graphite':
  gr_apache_24 => true
}

class {'statsd':
  backends     => ['./backends/graphite'],
  graphiteHost => 'localhost',
  require      => Class['nodejs']
}
