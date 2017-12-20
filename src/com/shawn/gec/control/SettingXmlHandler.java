package com.shawn.gec.control;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class SettingXmlHandler extends DefaultHandler {

    private StringBuilder _builder;
    private String _currentQName;
    private String _currentAttributeQName;

    Logger logger = LoggerFactory.getLogger(SettingCenter.class);

    @Override
    public void startDocument() throws SAXException {
        //System.out.println("Begin to parse document");
        super.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        //System.out.println("ended of parsing document");
        super.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        //System.out.println("now to parse element " + qName);

        _currentQName = qName;
        _builder = new StringBuilder();

        if (attributes != null) {
            for (int i=0; i<attributes.getLength(); i++) {
                logger.info("Reading XML attributes. QName:{} Value:{}", attributes.getQName(i), attributes.getValue(i));

                if (attributes.getQName(i).compareToIgnoreCase("mapName")==0) {
                    _currentAttributeQName = attributes.getValue(i);
                }
            }
        }

        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {

        if (qName.compareToIgnoreCase(_currentQName)==0) {
            String value = _builder.toString();
            logger.info("Reading XML, endElement. QName:{} Value:{}", qName, value);

            if (_currentQName.compareToIgnoreCase("FilePath") == 0) {
                SettingCenter.setExlFilePath(value);
            } else if (_currentQName.compareToIgnoreCase("DeltaFilePath") == 0) {
                SettingCenter.setExlDeltaFilePath(value);
            } else if (_currentQName.compareToIgnoreCase("DbFilePath") == 0) {
                SettingCenter.setDbFilePath(value);
            } else if (_currentQName.compareToIgnoreCase("OuputFilePath") == 0) {
                SettingCenter.setExlOutputPath(value);
            } else if (_currentQName.compareToIgnoreCase("GroupCapacity") == 0) {
                SettingCenter.setGroupCapacity(Integer.parseInt(value));
            } else if (_currentQName.compareToIgnoreCase("RoleName") == 0) {
                SettingCenter.addToRoleNameList(value);
            } else if (_currentQName.compareToIgnoreCase("Column") == 0) {
                if (_currentAttributeQName.compareToIgnoreCase("signupId") == 0) {
                    SettingCenter.COL_SIGNUP_ID = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("englishName") == 0) {
                    SettingCenter.COL_ENG_NAME = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("chineseName") == 0) {
                    SettingCenter.COL_CHN_NAME = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("gender") == 0) {
                    SettingCenter.COL_GENDER = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("homeTown") == 0) {
                    SettingCenter.COL_HOMETOWN = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("occupation") == 0) {
                    SettingCenter.COL_OCCUPATION = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("language") == 0) {
                    SettingCenter.COL_LANGUAGE = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("role") == 0) {
                    SettingCenter.COL_ROLE = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("experience") == 0) {
                    SettingCenter.COL_EXPERIENCE = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("wantToBeWith") == 0) {
                    SettingCenter.COL_WANNA_BE_WITH = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("mobile") == 0) {
                    SettingCenter.COL_MOBILE_NUMBER = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("qq") == 0) {
                    SettingCenter.COL_QQ = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("wechat") == 0) {
                    SettingCenter.COL_WECHAT = Integer.parseInt(value);
                } else if (_currentAttributeQName.compareToIgnoreCase("district") == 0) {
                    SettingCenter.COL_DISTRICT = Integer.parseInt(value);
                }
            }
        }

        super.endElement(uri, localName, qName);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {

        String content = new String(ch, start, length);
        _builder.append(content);

        super.characters(ch, start, length);
    }
}
