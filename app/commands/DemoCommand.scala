package commands

import com.netflix.hystrix.HystrixCommand.Setter
import com.netflix.hystrix.{ HystrixCommandProperties, HystrixCommandKey, HystrixCommandGroupKey, HystrixCommand }

object DemoCommand {
  private final val key = Setter
    .withGroupKey(HystrixCommandGroupKey.Factory.asKey("Hystrix demo"))
    .andCommandKey(HystrixCommandKey.Factory.asKey("HappyCommand"))
    .andCommandPropertiesDefaults(HystrixCommandProperties.Setter().withExecutionTimeoutInMilliseconds(1500))

  def apply() = new DemoCommand
}

class DemoCommand extends HystrixCommand[Int](DemoCommand.key) {
  def run(): Int = {
    42
  }
}
