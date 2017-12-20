package com.shawn.gec.test;


import com.shawn.gec.control.SettingCenter;
import org.junit.Test;



public class SettingXmlHandlerTest {

    @Test
    public void testHandler() {

        SettingCenter.ReadFromSettingFile();
        System.out.println(SettingCenter.instance);

    }

}
