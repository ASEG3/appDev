package com.g3.findmii;

import android.test.ActivityInstrumentationTestCase2;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;

import messageUtils.Message;

public class ApplicationTest extends ActivityInstrumentationTestCase2<MapsActivity> {

    private MapsActivity mapsActivity;
    private String browserKey;
    private String URL;
    private String[] realLocations;
    private Object[] falseLocations;
    private String[] realPrices;
    private Object[] falsePrices;
    private SecureRandom random = new SecureRandom();

    public ApplicationTest() {
        super(MapsActivity.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        mapsActivity = getActivity();
        testGetActivityResourceString();

        realLocations = new String[]{"Crawley", "Big Ben", "51.24423, -0.2323424"};
        ArrayList<String> mockLocations = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            mockLocations.add(randomString());
        }
        falseLocations = mockLocations.toArray();

        realPrices = new String[]{"£20,000", "£12,123", "111,111", "32", "1"};
        ArrayList<String> mockPrices = new ArrayList<>();
        for(int i = 0; i < 100; i++){
            mockPrices.add(randomIncorrectPrice());
        }
        falsePrices = mockPrices.toArray();
    }

    public String randomString() {
        return new BigInteger(130, random).toString(32);
    }

    public String randomIncorrectPrice() {
        return new BigInteger(130, random).toString(32);
    }

    public void testPreconditions() {
        assertNotNull("Maps Activity is null", mapsActivity);
    }

    public void testGetActivityResourceString() {
        browserKey = mapsActivity.getResources().getString(R.string.browser_key);
        URL = mapsActivity.URL;
        assertNotNull(browserKey);
        assertNotNull(URL);
    }

    @Test
    public void testSearch(){
        for(int i = 0; i < realLocations.length; i++) {
            try {
                String[] locationInfo = new SearchTask().execute(browserKey, realLocations[i]).get();
                assertTrue(locationInfo.length > 0);
                testLocation(locationInfo);
            }
            catch (Exception e){
                assertFalse("Search task failed: " + e.getMessage(), true);
            }
        }
        for(int i = 0; i < falseLocations.length; i++){
            try {
                String[] locationInfo = new SearchTask().execute(browserKey, (String) falseLocations[i]).get();
                assertTrue(locationInfo.length > 0);
                testLocation(locationInfo);
            }
            catch (Exception e){
                assertFalse("Search task failed: " + e.getMessage(), true);
            }
        }
    }

    public void testLocation(String[] location){
        if(location.length == 3){
            assertNotSame("Expected 1st field of returned search to be of type Double", new Exception(), Double.parseDouble(location[0]));
            assertNotSame("Expected 2nd field of returned search to be of type Double", new Exception(), Double.parseDouble(location[1]));
            assertNotSame("Expected 3rd field of returned search to be of type String", new String(), location[2]);
        }
        else if(location.length == 1){
            assertSame("Expected a value minimum value of double",Double.toString(Double.MIN_VALUE),location[0]);
        }
        else {
            assertFalse("Unexpected location array size found", true);
        }
    }

    @Test
    public void testBudgetRegex(){
        for(int i = 0; i < realPrices.length; i++){
            assertTrue("Expected example budget string " + i + " " + " to be true", mapsActivity.budgetRegexp(realPrices[i]));
        }
        for(int i = 0; i < falsePrices.length; i++){
            assertFalse("Expected example budget string " + i + " " + " to fail", mapsActivity.budgetRegexp((String) falsePrices[i]));
        }
    }

    @Test
    public void testServerTask(){
        try {
            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    try {
                        byte[] returnedMessage = new ServerTask().execute(URL, "51.117930736089", "-0.207110183901943", "N/A", "N/A", "N/A").get();
                        ByteArrayInputStream in = new ByteArrayInputStream(returnedMessage);
                        ObjectInputStream is = new ObjectInputStream(in);
                        Message convertedMessage = (Message) is.readObject();
                        ensureMessageArraySizes(convertedMessage);
                    }
                    catch (Exception e) {
                        assertFalse("Reached an exception: " + e.getMessage(), true);
                    }
                }
            });
        } catch (Throwable throwable) {
            assertFalse("Reached an exception: " + throwable.getMessage(), true);
        }
    }

    public void ensureMessageArraySizes(Message message){
        assertTrue("Expected array size to be less or equal to 10,000", message.getHouse().size() <= 10000);
        assertTrue("Expected array size to be less or equal to 10,000", message.getHouse().size() <= 10000);
    }



}