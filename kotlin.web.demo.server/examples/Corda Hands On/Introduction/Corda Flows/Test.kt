package com.template

import net.corda.node.internal.StartedNode
import net.corda.testing.node.MockNetwork
import net.corda.testing.node.MockNetwork.MockNode
import net.corda.testing.setCordappPackages
import net.corda.testing.unsetCordappPackages
import org.junit.After
import org.junit.Before
import org.junit.Test

class FlowTests {
    lateinit var network: MockNetwork
    lateinit var a: StartedNode<MockNode>
    lateinit var b: StartedNode<MockNode>

    @Before
    fun setup() {
        setCordappPackages("com.template")
        network = MockNetwork()
        val nodes = network.createSomeNodes(2)
        a = nodes.partyNodes[0]
        b = nodes.partyNodes[1]
        /* nodes.partyNodes.forEach {
             it.registerInitiatedFlow(Responder::class.java)
         }*/

        network.runNetwork()
    }

    @After
    fun tearDown() {
        network.stopNodes()
        unsetCordappPackages()
        System.exit(0)
    }

    @Test
    fun `dummy test`() = Unit
}