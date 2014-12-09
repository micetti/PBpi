package test;

import java.util.ArrayList;

import org.junit.Assert;
import org.junit.Test;

import utils.Props;
import utils.PushEntry;
import client.PushbulletDevice;

/**********************************************************************************************
 * In order to Test, no other activities must be happening on the used PB
 * account.
 */
public class JUnitTest {

	
	@Test
	public void initTest() {
		PushbulletDevice tester = new PushbulletDevice();
		Assert.assertEquals(Props.deviceName(), tester.getNickname());
	}

	@Test
	public void pushAndReadTest() {
		PushbulletDevice pusher = new PushbulletDevice();
		// TODO: FIX (UTF-8?) NEVER TRY TO PUSH UMLAUTS ÄÖÜ...
		pusher.push(new PushEntry("TestA", "Streichholzschaechtelchen",
				pusher.getIden(), pusher.getIden()));
		// Push with faulty target device. -> nothing will/should happen.
		pusher.push(new PushEntry("TestB", "Streichholzschaechtelchen",
				"ujC6kIYE4xEsjz6ArhhPB6", pusher.getIden()));
		// Push with faulty source device. -> nothing will/should happen.
		pusher.push(new PushEntry("TestC", "Streichholzschaechtelchen",
				 pusher.getIden(), "ujC6kIYE4xEsjz6ArhhPB6"));
		
		// It has same nick but new iden. Only the new push should appear when reading; 
		PushbulletDevice reader = new PushbulletDevice();
		reader.push(new PushEntry("TestD", "Streichholzschaechtelchen",
				reader.getIden(), reader.getIden()));
		ArrayList<PushEntry> pushList = reader.read();
		Assert.assertEquals(1, pushList.size());
		Assert.assertEquals("TestD", pushList.get(0).title);
		Assert.assertEquals("Streichholzschaechtelchen", pushList.get(0).body);
		Assert.assertEquals(reader.getIden(), pushList.get(0).sourceDevice);
		Assert.assertEquals(reader.getIden(), pushList.get(0).targetDevice);
	}
	
	@Test
	public void deleteTest() {
		PushbulletDevice deleter = new PushbulletDevice();
		deleter.push(new PushEntry("DeleteTest", "Streichholzschaechtelchen",
				deleter.getIden(), deleter.getIden()));
		ArrayList<PushEntry> pushList = deleter.read();
		Assert.assertEquals(1, pushList.size());
		Assert.assertEquals("DeleteTest", pushList.get(0).title);
		Assert.assertEquals("Streichholzschaechtelchen", pushList.get(0).body);
		deleter.deletePush(pushList.get(0));
		pushList = deleter.read();
		Assert.assertEquals(0, pushList.size());
	}
}
