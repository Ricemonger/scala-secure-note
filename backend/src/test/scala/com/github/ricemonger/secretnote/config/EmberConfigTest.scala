package com.github.ricemonger.secretnote.config

import com.comcast.ip4s.{Host, Port}
import pureconfig.ConfigSource
import munit.FunSuite

class EmberConfigTest extends FunSuite {

  test("hostReader successfully parses valid address") {
    val source = ConfigSource.string("""host = "127.0.0.1"""")
    val result = source.at("host").load[Host](using EmberConfig.hostReader)

    assert(result.isRight)
    assertEquals(result.toOption.get.toString, "127.0.0.1")
  }

  test("hostReader fails on invalid host string") {
    val source = ConfigSource.string("""host = "invalid-host!@#"""")
    val result = source.at("host").load[Host](using EmberConfig.hostReader)

    assert(result.isLeft)
  }

  test("portReader successfully parses valid port number") {
    val source = ConfigSource.string("""port = 8080""")
    val result = source.at("port").load[Port](using EmberConfig.portReader)

    assert(result.isRight)
    assertEquals(result.toOption.get.value, 8080)
  }

  test("portReader fails on invalid port integer") {
    val source = ConfigSource.string("""port = 999999""")
    val result = source.at("port").load[Port](using EmberConfig.portReader)

    assert(result.isLeft)
  }
}