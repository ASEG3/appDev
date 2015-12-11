package com.g3.findmii;

import android.test.ActivityInstrumentationTestCase2;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ObjectInputStream;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;

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
    public HashMap<Double, Double> sampleLatLngs = new HashMap<>();
    public Double tmp = 0d;

    public ApplicationTest() {
        super(MapsActivity.class);
    }

    /**
     * Initiaizes all of the variables ready for testing
     * @throws Exception
     */

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
        setUpRandomLatLngs();
    }

    /**
     * Generates a random String
     * @return a random String
     */
    public String randomString() {
        return new BigInteger(130, random).toString(32);
    }

    /**
     * Generates a random Integer parsed as a string
     * @return Random Integer parsed as a string
     */
    public String randomIncorrectPrice() {
        return new BigInteger(130, random).toString(32);
    }

    /**
     * Ensures that the Map Activity returned is not null.
     */
    public void testPreconditions() {
        assertNotNull("Maps Activity is null", mapsActivity);
    }

    /**
     * Ensures that the resource key used is not null.
     */
    public void testGetActivityResourceString() {
        browserKey = mapsActivity.getResources().getString(R.string.browser_key);
        URL = mapsActivity.URL;
        assertNotNull(browserKey);
        assertNotNull(URL);
    }

    /**
     * Tests the search function within the application against real and fake locations.
     */
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

    /**
     * Ensures that the returned string arrays of each search are of the expected type.
     * @param location
     */
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

    /**
     * Tests the RegExp that protects against invalid user entries
     */
    @Test
    public void testBudgetRegex(){
        for(int i = 0; i < realPrices.length; i++){
            assertTrue("Expected example budget string " + i + " " + " to be true", mapsActivity.budgetRegexp(realPrices[i]));
        }
        for(int i = 0; i < falsePrices.length; i++){
            assertFalse("Expected example budget string " + i + " " + " to fail", mapsActivity.budgetRegexp((String) falsePrices[i]));
        }
    }

    /**
     * Tests the server task to ensure the correct information is passed back, and is parsed correctly.
     */
    @Test
    public void testServerTask(){
        for(Double current : sampleLatLngs.keySet()) {
            tmp = current;
            try {
                runTestOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            byte[] returnedMessage = new ServerTask().execute(URL, String.valueOf(tmp), String.valueOf(sampleLatLngs.get(tmp)), "N/A", "N/A", "N/A").get();
                            ByteArrayInputStream in = new ByteArrayInputStream(returnedMessage);
                            ObjectInputStream is = new ObjectInputStream(in);
                            Message convertedMessage = (Message) is.readObject();
                            ensureMessageArraySizes(convertedMessage);
                        } catch (Exception e) {
                            assertFalse("Reached an exception: " + e.getMessage(), true);
                        }
                    }
                });
            } catch (Throwable throwable) {
                assertFalse("Reached an exception: " + throwable.getMessage(), true);
            }
        }
    }

    /**
     * Ensures the arrays within the returned message from the server are less than 10000 protecting against buffer overflow
     * @param message
     */
    public void ensureMessageArraySizes(Message message){
        assertTrue("Expected array size to be less or equal to 10,000", message.getHouse().size() <= 10000);
        assertTrue("Expected array size to be less or equal to 10,000", message.getHouses().size() <= 10000);
    }


    public void setUpRandomLatLngs(){
        sampleLatLngs.put(51.76969704,-0.231229418);
        sampleLatLngs.put(51.8078293,-0.329405111);
        sampleLatLngs.put(51.82202871,-0.157325364);
        sampleLatLngs.put(51.82818375,-0.218896646);
        sampleLatLngs.put(51.79989938,-0.169424282);
        sampleLatLngs.put(52.44684224,-1.854105631);
        sampleLatLngs.put(52.43893009,-1.886068029);
//        sampleLatLngs.put(52.42819601,-1.895860983);
//        sampleLatLngs.put(52.48863144,-1.942285392);
//        sampleLatLngs.put(52.5242198,-1.84774797);
//        sampleLatLngs.put(52.46342872,-1.82124318);
//        sampleLatLngs.put(52.46796525,-1.781657186);
//        sampleLatLngs.put(52.43814153,-1.848250255);
//        sampleLatLngs.put(52.43472682,-1.925028304);
//        sampleLatLngs.put(52.42986194,-1.938449668);
//        sampleLatLngs.put(52.41006207,-1.979859868);
//        sampleLatLngs.put(52.43875085,-1.994395494);
//        sampleLatLngs.put(52.50288445,-1.808929714);
//        sampleLatLngs.put(52.39902473,-1.955732137);
//        sampleLatLngs.put(52.5497638,-1.927753107);
//        sampleLatLngs.put(52.55170166,-1.936584041);
//        sampleLatLngs.put(52.5517418,-1.874155256);
//        sampleLatLngs.put(52.37927265,-1.996709092);
//        sampleLatLngs.put(52.51785761,-1.643724324);
//        sampleLatLngs.put(52.32862006,-2.050961547);
//        sampleLatLngs.put(52.47303466,-2.055516154);
//        sampleLatLngs.put(52.47612149,-2.048070317);
//        sampleLatLngs.put(52.486609,-2.032928442);
//        sampleLatLngs.put(52.48995717,-1.986436485);
//        sampleLatLngs.put(52.49072732,-2.029470537);
//        sampleLatLngs.put(52.58510333,-1.883210696);
//        sampleLatLngs.put(52.53744886,-1.699664412);
//        sampleLatLngs.put(52.55977572,-1.789595425);
//        sampleLatLngs.put(52.61357358,-1.678600687);
//        sampleLatLngs.put(52.5670053,-1.757561077);
//        sampleLatLngs.put(52.6397021,-1.693792984);
//        sampleLatLngs.put(52.41575806,-1.811878947);
//        sampleLatLngs.put(52.41258579,-1.771683376);
//        sampleLatLngs.put(52.29249055,-1.783619599);
//        sampleLatLngs.put(52.32109976,-1.910665875);
//        sampleLatLngs.put(51.3899835,-2.385497933);
//        sampleLatLngs.put(51.23417338,-2.309957248);
//        sampleLatLngs.put(51.28664362,-2.280370075);
//        sampleLatLngs.put(51.29574256,-2.235893113);
//        sampleLatLngs.put(51.34368539,-2.245241838);
//        sampleLatLngs.put(51.33214324,-2.47947327);
//        sampleLatLngs.put(50.93980196,-2.628751349);
//        sampleLatLngs.put(50.99903501,-2.581883518);
//        sampleLatLngs.put(51.19109292,-2.543946566);
//        sampleLatLngs.put(53.76667839,-2.417865192);
//        sampleLatLngs.put(53.76105799,-2.487600073);
//        sampleLatLngs.put(53.80176417,-2.238702973);
//        sampleLatLngs.put(53.80324206,-2.24126224);
//        sampleLatLngs.put(53.81151252,-2.201520632);
//        sampleLatLngs.put(53.76371972,-2.18132845);
//        sampleLatLngs.put(53.78108193,-2.272791528);
//        sampleLatLngs.put(53.78116443,-2.316027817);
//        sampleLatLngs.put(53.81190968,-2.346283697);
//        sampleLatLngs.put(53.83255136,-2.262824424);
//        sampleLatLngs.put(53.8407137,-2.20714721);
//        sampleLatLngs.put(53.84312872,-2.203496395);
//        sampleLatLngs.put(53.74967859,-1.663374176);
//        sampleLatLngs.put(53.84383813,-1.836732838);
//        sampleLatLngs.put(53.86602045,-1.850742965);
//        sampleLatLngs.put(53.84268185,-1.777161299);
//        sampleLatLngs.put(53.83239465,-1.765090828);
//        sampleLatLngs.put(53.83672679,-1.789303943);
//        sampleLatLngs.put(53.7291369,-1.671692642);
//        sampleLatLngs.put(53.91931038,-2.018173693);
//        sampleLatLngs.put(54.05090628,-2.309351474);
//        sampleLatLngs.put(53.79283633,-1.72584376);
//        sampleLatLngs.put(53.77837778,-1.708122747);
//        sampleLatLngs.put(53.76276711,-1.780572426);
//        sampleLatLngs.put(53.81039185,-1.799641045);
//        sampleLatLngs.put(50.75074647,-1.896158903);
//        sampleLatLngs.put(50.75293089,-1.895275194);
//        sampleLatLngs.put(50.74942619,-1.93606321);
//        sampleLatLngs.put(50.72328005,-1.969546159);
//        sampleLatLngs.put(50.76632245,-1.991993237);
//        sampleLatLngs.put(50.7538077,-2.222693729);
//        sampleLatLngs.put(50.82695268,-1.889814236);
//        sampleLatLngs.put(50.74278637,-1.723823825);
//        sampleLatLngs.put(50.74540117,-1.704617702);
//        sampleLatLngs.put(50.74590873,-1.666062484);
//        sampleLatLngs.put(53.60442951,-2.433836742);
//        sampleLatLngs.put(53.54573993,-2.430112066);
//        sampleLatLngs.put(53.60105671,-2.542300844);
//        sampleLatLngs.put(53.60502676,-2.293613211);
//        sampleLatLngs.put(53.57716888,-2.294884943);
//        sampleLatLngs.put(53.56825086,-2.295532522);
//        sampleLatLngs.put(50.82772496,-0.395829816);
//        sampleLatLngs.put(50.83718767,-0.408443645);
//        sampleLatLngs.put(50.75821742,0.275514578);
//        sampleLatLngs.put(50.80643237,0.267296435);
//        sampleLatLngs.put(50.79966527,0.31871297);
//        sampleLatLngs.put(50.78108255,0.087409238);
//        sampleLatLngs.put(50.81715942,0.250253975);
//        sampleLatLngs.put(50.83876029,-0.183004774);
//        sampleLatLngs.put(50.85080964,-0.186358221);
//        sampleLatLngs.put(50.88237291,-0.305828574);
//        sampleLatLngs.put(50.92165269,-0.134019305);
//        sampleLatLngs.put(50.87587597,0.00850027);
//        sampleLatLngs.put(50.94564076,-0.021122965);
//        sampleLatLngs.put(51.37420847,0.048454354);
//        sampleLatLngs.put(51.40476251,0.097972407);
//        sampleLatLngs.put(51.41491075,0.047914907);
//        sampleLatLngs.put(51.4066355772641,-2.57436375095294);
//        sampleLatLngs.put(51.4169204508492,-2.53481779679373);
//        sampleLatLngs.put(51.4550208622483,-2.51204862597507);
//        sampleLatLngs.put(51.4734053287251,-2.53494563703803);
//        sampleLatLngs.put(51.4786314554787,-2.68336593054103);
//        sampleLatLngs.put(51.4755524559785,-2.80703374994475);
//        sampleLatLngs.put(51.3552869317005,-2.91931841071883);
//        sampleLatLngs.put(51.3511642093098,-2.93801855330759);
//        sampleLatLngs.put(51.3414726141779,-2.96873091947342);
//        sampleLatLngs.put(51.3367239674351,-2.9699227789586);
//        sampleLatLngs.put(51.3351421470017,-2.98062683931644);
//        sampleLatLngs.put(51.4318433387198,-2.5987026728851);
//        sampleLatLngs.put(51.4317405034812,-2.60819533175232);
//        sampleLatLngs.put(51.4306815445388,-2.4733123129384);
//        sampleLatLngs.put(51.4008313730336,-2.46830332103329);
//        sampleLatLngs.put(51.5323486356042,-2.47786969002707);
//        sampleLatLngs.put(51.5242426888368,-2.42537583583883);
//        sampleLatLngs.put(51.4301661086634,-2.58900007145235);
//        sampleLatLngs.put(51.3308000117987,-2.71466584842122);
//        sampleLatLngs.put(51.4365406490767,-2.7538783566347);
//        sampleLatLngs.put(51.4664636293682,-2.59469315097923);
//        sampleLatLngs.put(51.4520885517665,-2.62022344349631);
//        sampleLatLngs.put(54.6543946077472,-3.51821237327618);
//        sampleLatLngs.put(54.7131840840607,-3.50370545865585);
//        sampleLatLngs.put(54.8858834877617,-2.93941043458721);
//        sampleLatLngs.put(54.88928404768,-2.94705075419095);
//        sampleLatLngs.put(54.9151807101941,-2.95204253787629);
//        sampleLatLngs.put(54.7296562513962,-2.99349677884635);
//        sampleLatLngs.put(54.9901590232157,-2.56950291986039);
//        sampleLatLngs.put(54.8106191837025,-2.43724089126243);
//        sampleLatLngs.put(52.147309951341,0.13227838966521);
//        sampleLatLngs.put(52.2405629802812,0.10586997702578);
//        sampleLatLngs.put(51.4646064433698,-3.17713587817539);
//        sampleLatLngs.put(51.4880056732123,-3.2008554530532);
//        sampleLatLngs.put(51.5093017516691,-3.21016217638267);
//        sampleLatLngs.put(51.5237413488865,-3.18475774184495);
//        sampleLatLngs.put(51.5096959767478,-3.13568790045281);
//        sampleLatLngs.put(51.529098931897,-3.6840922862127);
//        sampleLatLngs.put(51.7035101953496,-3.43053073701603);
//        sampleLatLngs.put(51.7169232038311,-3.47151596173451);
//        sampleLatLngs.put(51.6837114499051,-3.37702102863938);
//        sampleLatLngs.put(51.751486573186,-3.36817313913547);
//        sampleLatLngs.put(51.7516277082017,-3.37226274769478);
//        sampleLatLngs.put(51.7412687601517,-3.36690846730736);
//        sampleLatLngs.put(51.4914190247652,-3.25444232264211);
//        sampleLatLngs.put(51.4704089660223,-3.25068398146577);
//        sampleLatLngs.put(51.4442861896879,-3.26418673534125);
//        sampleLatLngs.put(51.4032817103678,-3.18776589722874);
//        sampleLatLngs.put(51.6572466298267,-3.25190424245212);
//        sampleLatLngs.put(51.669600153264,-3.24439271529675);
//        sampleLatLngs.put(53.3983845189097,-3.05016396062703);
//        sampleLatLngs.put(53.3784388448877,-3.01789356705713);
//        sampleLatLngs.put(53.3785472054762,-3.04488019495728);
//        sampleLatLngs.put(53.3986363010532,-3.06032216598);
//        sampleLatLngs.put(53.3975845439391,-3.10006090279304);
//        sampleLatLngs.put(53.3640811357196,-3.17559956812693);
//        sampleLatLngs.put(53.3807602820153,-3.12574078577547);
//        sampleLatLngs.put(53.207814720963,-3.04096044218699);
//        sampleLatLngs.put(53.2185195822885,-3.05845836020469);
//        sampleLatLngs.put(53.3386488143666,-3.10431046771899);
//        sampleLatLngs.put(53.3328732669352,-2.98471725884193);
//        sampleLatLngs.put(53.3090837649941,-2.97761090841632);
//        sampleLatLngs.put(51.7879803772269,0.137212216752203);
//        sampleLatLngs.put(51.7157533902405,0.501500599112254);
//        sampleLatLngs.put(51.7227759965772,0.474376304943413);
//        sampleLatLngs.put(51.8984917980076,0.194651282304553);
//        sampleLatLngs.put(51.6537497981081,0.614790337405989);
//        sampleLatLngs.put(51.9102077895269,0.282620513739668);
//        sampleLatLngs.put(51.9685449285496,0.446765687710464);
//        sampleLatLngs.put(51.894732343588,0.910733391620114);
//        sampleLatLngs.put(52.0564994534911,0.776623684281439);
//        sampleLatLngs.put(52.0746937438085,0.715624299878997);
//        sampleLatLngs.put(51.7807877840379,1.13829198288794);
//        sampleLatLngs.put(51.8035446365468,1.15066712009853);
//        sampleLatLngs.put(51.8098136276778,1.14865243631105);
//        sampleLatLngs.put(51.9012324557278,0.814385813024857);
//        sampleLatLngs.put(51.8181091944133,1.02843041604282);
//        sampleLatLngs.put(51.9597523356667,0.772720278449858);
//        sampleLatLngs.put(51.3392962624684,-0.006168545051991);
//        sampleLatLngs.put(51.347285200566,-0.082849102743155);
//        sampleLatLngs.put(51.3375090998577,-0.055519383150064);
//        sampleLatLngs.put(51.3295863002036,-0.077300160868189);
//        sampleLatLngs.put(51.2998239331561,-0.072619513906633);
//        sampleLatLngs.put(51.2876894010735,-0.10381813074527);
//        sampleLatLngs.put(51.273814878716,-0.078539744281561);
//        sampleLatLngs.put(51.2850271249841,-0.036154285910637);
//        sampleLatLngs.put(51.3406826929506,-0.142451291147321);
//        sampleLatLngs.put(51.3593601306648,1.44054565308197);
//        sampleLatLngs.put(51.3379133101887,1.38789957749621);
//        sampleLatLngs.put(51.1557745581633,1.37026685304795);
//        sampleLatLngs.put(51.0890530522152,1.10254747357371);
//        sampleLatLngs.put(51.2843988193838,1.0312789069887);
//        sampleLatLngs.put(51.0827774122409,1.18631209742947);
//        sampleLatLngs.put(51.3243785410017,1.20154756595985);
//        sampleLatLngs.put(51.3651774781922,1.07374090880593);
//        sampleLatLngs.put(51.366637530464,1.10125452573771);
//        sampleLatLngs.put(51.3851256810902,1.38178534684046);
//        sampleLatLngs.put(51.3830722457575,1.38249634184254);
//        sampleLatLngs.put(51.3889538543046,1.38658215073204);
//        sampleLatLngs.put(51.3864497116489,1.3807904666314);
//        sampleLatLngs.put(52.6234691319686,-1.41110492373523);
//        sampleLatLngs.put(52.6587226250121,-1.38886900289067);
//        sampleLatLngs.put(52.3944875659166,-1.47487926713259);
//        sampleLatLngs.put(52.3960758028536,-1.50761847490192);
//        sampleLatLngs.put(52.2829030224626,-1.52658105817215);
//        sampleLatLngs.put(52.2688366223186,-1.60860354307796);
//        sampleLatLngs.put(52.1864639796378,-1.71061877655098);
//        sampleLatLngs.put(52.403301178596,-1.58164878133685);
//        sampleLatLngs.put(52.1701566509946,-1.42584978190222);
//        sampleLatLngs.put(52.4142956514302,-1.52891051208917);
//        sampleLatLngs.put(52.4284183049508,-1.48760974066939);
//        sampleLatLngs.put(52.4430445034093,-1.49370743386051);
//        sampleLatLngs.put(52.3345930095551,-1.57767406300818);
//        sampleLatLngs.put(52.3488239579843,-1.57750918042611);
//        sampleLatLngs.put(53.1915841852054,-2.44287125586429);
//        sampleLatLngs.put(53.1382856905526,-2.40052713732722);
//        sampleLatLngs.put(53.2036442244501,-2.36713744629079);
//        sampleLatLngs.put(53.0238769665428,-2.60552009387639);
//        sampleLatLngs.put(53.1941779063148,-2.51121090652241);
//        sampleLatLngs.put(53.2121018389865,-2.58175752120353);
//        sampleLatLngs.put(51.4372101972275,0.350917750474487);
//        sampleLatLngs.put(51.4221908773765,0.099475327683211);
//        sampleLatLngs.put(51.462493711966,0.087576012065847);
//        sampleLatLngs.put(51.4906218387931,0.129167042394413);
//        sampleLatLngs.put(51.4471265303461,0.13639677442932);
//        sampleLatLngs.put(51.4631010510291,0.144600355600507);
//        sampleLatLngs.put(52.7885681945178,-1.56613233426407);
//        sampleLatLngs.put(52.739512735569,-1.55660673876821);
//        sampleLatLngs.put(52.6825500298201,-1.53441821999038);
//        sampleLatLngs.put(52.8214256208921,-1.64785199940383);
//        sampleLatLngs.put(52.9354806234762,-1.4950198129527);
//        sampleLatLngs.put(52.878461269473,-1.49300747739202);
//        sampleLatLngs.put(53.0978030582696,-1.33157701924625);
//        sampleLatLngs.put(53.1464068291986,-1.33308052830574);
//        sampleLatLngs.put(53.1279026035186,-1.40880847440943);
//        sampleLatLngs.put(53.0086090977665,-1.72196638430196);
//        sampleLatLngs.put(52.9816017477408,-1.31943006503953);
//        sampleLatLngs.put(54.7851053736469,-1.64140248622205);
//        sampleLatLngs.put(54.7793929363995,-1.63636906707905);
//        sampleLatLngs.put(54.8641275686493,-1.74655057153013);
//        sampleLatLngs.put(54.8748447010419,-1.76256588603446);
//        sampleLatLngs.put(54.403489588892,-1.73990670115097);
//        sampleLatLngs.put(54.648601433802,-1.74572233412977);
//        sampleLatLngs.put(54.5349901923774,-1.56306533981341);
//        sampleLatLngs.put(54.6326063188562,-1.63682683121181);
//        sampleLatLngs.put(54.3372888402818,-1.44026227332361);
//        sampleLatLngs.put(54.309201994896,-1.82231194407794);
//        sampleLatLngs.put(53.434759986563,-1.10458110840607);
//        sampleLatLngs.put(53.5947005916921,-0.6515398460522);
//        sampleLatLngs.put(53.5294159791587,-0.651788923519827);
//        sampleLatLngs.put(53.5380724532342,-1.11367485413366);
//        sampleLatLngs.put(53.3059681325699,-0.944237833274845);
//        sampleLatLngs.put(53.5320491351565,-1.04918270233992);
//        sampleLatLngs.put(53.5277293050677,-1.04401456922545);
//        sampleLatLngs.put(53.5052828728675,-0.052673665358374);
//        sampleLatLngs.put(53.5042762168771,-0.147702773587615);
//        sampleLatLngs.put(53.5451096973141,-0.400073812098285);
//        sampleLatLngs.put(53.501801213062,-1.15056917880208);
//        sampleLatLngs.put(53.6053618067762,-1.18854280390861);
//        sampleLatLngs.put(53.5715938334756,-1.01079973501569);
//        sampleLatLngs.put(50.868300913622,-2.25755803250181);
//        sampleLatLngs.put(50.8561450488841,-2.16776019903826);
//        sampleLatLngs.put(50.6975124338963,-2.49255141901001);
//        sampleLatLngs.put(50.6495888656142,-2.45977192328862);
//        sampleLatLngs.put(50.6168515867743,-2.4651205562509);
//        sampleLatLngs.put(50.7249251796924,-2.94212944703872);
//        sampleLatLngs.put(50.7290382065297,-2.96046137205941);
//        sampleLatLngs.put(52.3931443646188,-2.24803997075849);
//        sampleLatLngs.put(52.3850269242526,-2.25616346721277);
//        sampleLatLngs.put(52.4059058924145,-2.57467614759137);
//        sampleLatLngs.put(52.5319358213138,-2.1178439733969);
//        sampleLatLngs.put(52.4707865039654,-2.14216129459275);
//        sampleLatLngs.put(52.5039706314112,-2.16143500757167);
//        sampleLatLngs.put(51.5125965916485,-0.053837460213143);
//        sampleLatLngs.put(51.5611704755415,-0.016013210286235);
//        sampleLatLngs.put(51.5131527897848,-0.027381787963534);
//        sampleLatLngs.put(51.5046497941111,0.020969761286943);
//        sampleLatLngs.put(51.5993676867681,-0.000457952459513);
//        sampleLatLngs.put(51.5815353575231,-0.020077112475909);
//        sampleLatLngs.put(51.5738472698078,-0.026515722084794);
//        sampleLatLngs.put(51.5306820918992,-0.029680677651972);
//        sampleLatLngs.put(51.765467813216,-0.011097062701734);
//        sampleLatLngs.put(51.6573389564152,-0.087705279870574);
//        sampleLatLngs.put(51.6556330985233,-0.16335942212186);
//        sampleLatLngs.put(51.6433683915324,-0.207633825413313);
//        sampleLatLngs.put(50.7212609451505,-3.49761750450746);
//        sampleLatLngs.put(50.6768753184588,-3.29855008394585);
//        sampleLatLngs.put(50.6817072823796,-3.24810824947766);
//        sampleLatLngs.put(50.7384307234047,-3.32040470759);
//        sampleLatLngs.put(50.853247845123,-3.03603208839571);
//        sampleLatLngs.put(50.8140173171967,-4.35688428543687);
//        sampleLatLngs.put(51.1128045411366,-4.07401914012817);
//        sampleLatLngs.put(51.0699235676731,-4.04549391537676);
//        sampleLatLngs.put(51.0514208821262,-4.17994437130407);
//        sampleLatLngs.put(50.7332373846806,-3.67679187613654);
//        sampleLatLngs.put(50.5840208802806,-3.46304360530039);
//        sampleLatLngs.put(53.8496271876758,-2.9895887484);
//        sampleLatLngs.put(53.7633036531312,-3.04356484426034);
//        sampleLatLngs.put(51.8485816620664,-2.25517967407376);
//        sampleLatLngs.put(51.5954486906588,-2.48052417350153);
//        sampleLatLngs.put(51.9372912050951,-2.40140338169795);
//        sampleLatLngs.put(51.8525962451137,-2.22189698332978);
//        sampleLatLngs.put(51.8452276054784,-2.19429460789175);
//        sampleLatLngs.put(51.7404312923279,-2.22760292449793);
//        sampleLatLngs.put(51.8896992451244,-2.08700502499399);
//        sampleLatLngs.put(51.8828670820665,-2.10760733339275);
//        sampleLatLngs.put(51.8889152327147,-2.12821106068289);
//        sampleLatLngs.put(51.8958707516709,-2.04968322693864);
//        sampleLatLngs.put(51.9313515510616,-1.72044930071046);
//        sampleLatLngs.put(51.8772723842008,-1.70246662655994);
//        sampleLatLngs.put(51.6965550926899,-2.23189667622497);
//        sampleLatLngs.put(51.7341857307928,-2.19588815253429);
//        sampleLatLngs.put(51.1839699784443,-0.749488680496923);
//        sampleLatLngs.put(51.2600075226156,-0.720196493540367);
//        sampleLatLngs.put(51.2665708588116,-0.725045391349874);
//        sampleLatLngs.put(51.2945233929386,-0.742931001974539);
//        sampleLatLngs.put(51.3317182605981,-0.701981037324418);
//        sampleLatLngs.put(51.3252663163671,-0.705636388651582);
//        sampleLatLngs.put(51.2988569007798,-0.555738551286829);
//        sampleLatLngs.put(51.1261728966879,-0.756837440989179);
//        sampleLatLngs.put(51.0690122718978,-0.725556353671981);
//        sampleLatLngs.put(50.9852337870116,-0.736884083510403);
//        sampleLatLngs.put(51.2627398631462,-0.602879068752833);
//        sampleLatLngs.put(51.0802377920692,-0.80325260180128);
//        sampleLatLngs.put(51.3443107971446,-0.776674896242259);
//        sampleLatLngs.put(51.3563070308752,-0.776743203737181);
//        sampleLatLngs.put(51.3537945051341,-0.813990606869207);
//        sampleLatLngs.put(51.2830919102044,-0.839399633369497);
//        sampleLatLngs.put(51.2690704465168,-0.839136445942156);
//        sampleLatLngs.put(51.575109791632,-0.342352843535942);
//        sampleLatLngs.put(51.5623904536114,-0.376777013743133);
//        sampleLatLngs.put(51.5819780284997,-0.305081101382632);
//        sampleLatLngs.put(51.5944983952589,-0.300327159467321);
//        sampleLatLngs.put(51.6150513331535,-0.304456012183064);
//        sampleLatLngs.put(53.6989254940225,-1.78593272858354);
//        sampleLatLngs.put(54.0056851492971,-1.53063386804819);
//        sampleLatLngs.put(54.1863174688059,-1.49494016802889);
//        sampleLatLngs.put(54.014942724453,-1.47214180421692);
//        sampleLatLngs.put(51.6414641172912,-0.786775202948625);
//        sampleLatLngs.put(51.6426337347038,-0.792076796706307);
//        sampleLatLngs.put(51.8064586232443,-0.942188730202086);
//        sampleLatLngs.put(51.8130168807849,-0.807227268882777);
//        sampleLatLngs.put(51.7874494102294,-0.755577289962537);
//        sampleLatLngs.put(51.7272166223108,-0.834679896756114);
//        sampleLatLngs.put(51.7198615074414,-0.528510816618376);
//        sampleLatLngs.put(51.7421012706507,-0.456540748148469);
//        sampleLatLngs.put(51.7613955378202,-0.56274913144753);
//        sampleLatLngs.put(51.7475358710669,-0.577993512421394);
//        sampleLatLngs.put(51.7083152361997,-0.617288185217497);
//        sampleLatLngs.put(52.0612950487952,-2.73594088334528);
//        sampleLatLngs.put(52.1829455829878,-2.90085436379111);
//        sampleLatLngs.put(53.838665666344,-0.418829681925333);
//        sampleLatLngs.put(53.7608008380792,-0.361105988141642);
//        sampleLatLngs.put(53.7546262455496,-0.377030067853724);
//        sampleLatLngs.put(53.7682676604654,-0.394756904952386);
//        sampleLatLngs.put(53.7767805262571,-0.384734643814087);
//        sampleLatLngs.put(53.786909786647,-0.389977190432036);
//        sampleLatLngs.put(53.7496702973621,-0.327127031288302);
//        sampleLatLngs.put(53.66564393452,-1.95195673863559);
//        sampleLatLngs.put(53.7330280664859,-1.99224708156795);
//        sampleLatLngs.put(51.6509837949124,0.069655087909874);
//        sampleLatLngs.put(51.5413157867962,0.070313859788637);
//        sampleLatLngs.put(51.5607293650129,0.098752035841604);
//        sampleLatLngs.put(51.6055026433626,0.055642729861706);
//        sampleLatLngs.put(52.0548141281917,1.15057273369591);
//        sampleLatLngs.put(51.9766815030217,1.37382985235427);
//        sampleLatLngs.put(52.0748314245473,1.28820800812541);
//        sampleLatLngs.put(52.2111036181602,1.05299518547872);
//        sampleLatLngs.put(52.1874965748557,1.01169371836939);
//        sampleLatLngs.put(52.1857332233454,1.52639423199866);
//        sampleLatLngs.put(52.4343544298916,1.3355005923012);
//        sampleLatLngs.put(52.3478367005324,1.25544408510967);
//        sampleLatLngs.put(52.3747783386285,1.11760500412951);
//        sampleLatLngs.put(52.3817765699033,1.08088583442758);
//        sampleLatLngs.put(52.2352567054027,0.699434619603686);
//        sampleLatLngs.put(52.0580971614879,1.24821027525708);
//        sampleLatLngs.put(52.1009323967686,1.10732403180242);
//        sampleLatLngs.put(52.1350015408079,1.06159703762726);
//        sampleLatLngs.put(52.1541991674645,1.21309533360409);
//        sampleLatLngs.put(52.0241765916471,0.961573449410977);
//        sampleLatLngs.put(51.3861587750714,-0.422843929160345);
//        sampleLatLngs.put(51.3458790323873,-0.462551038658962);
//        sampleLatLngs.put(51.3721481078637,-0.457935880637649);
//        sampleLatLngs.put(51.3676826311821,-0.450041061426036);
//        sampleLatLngs.put(51.3763331944201,-0.434475245071453);
//        sampleLatLngs.put(51.3384906134973,-0.246427379517003);
//        sampleLatLngs.put(51.3265123621908,-0.259687356974085);
//        sampleLatLngs.put(51.4181435127277,-0.289402781156937);
//        sampleLatLngs.put(51.308215067859,-0.28118343404454);
//        sampleLatLngs.put(51.308482928122,-0.303871160242378);
//        sampleLatLngs.put(51.3993827625407,-0.261738987253114);
//        sampleLatLngs.put(51.3811961418173,-0.232638427009993);
//        sampleLatLngs.put(51.3877559870486,-0.309723167114776);
//        sampleLatLngs.put(53.3988285186826,-2.92361343922052);
//        sampleLatLngs.put(53.3880063211056,-2.9465046201513);
//        sampleLatLngs.put(53.4680951788749,-2.98456114058756);
//        sampleLatLngs.put(53.4900192481801,-3.026186101222);
//        sampleLatLngs.put(53.4868136364051,-3.01314752553126);
//        sampleLatLngs.put(53.4958036905711,-3.00555338677587);
//        sampleLatLngs.put(53.3964394747791,-2.86330032601919);
//        sampleLatLngs.put(53.4939016340372,-2.97254238997755);
//        sampleLatLngs.put(53.487467137541,-2.91429540446501);
//        sampleLatLngs.put(53.4265149306991,-2.79755621012913);
//        sampleLatLngs.put(53.4133842437011,-2.79051015868116);
//        sampleLatLngs.put(53.4179788446209,-2.83004739900664);
//        sampleLatLngs.put(53.5544016294293,-2.90779823024251);
//        sampleLatLngs.put(53.6341017481994,-2.81435248849461);
//        sampleLatLngs.put(53.3913739878844,-2.96375196398059);
//        sampleLatLngs.put(53.4639880751734,-2.96016947914546);
//        sampleLatLngs.put(53.466378137591,-2.9456416640882);
//        sampleLatLngs.put(54.0447321530768,-2.80412715752879);
//        sampleLatLngs.put(54.1676908119368,-2.93055057846054);
//        sampleLatLngs.put(54.1752114810838,-2.92498968410897);
//        sampleLatLngs.put(54.1805003804712,-3.10276979932354);
//        sampleLatLngs.put(54.1133453458087,-3.1901871104258);
//        sampleLatLngs.put(54.1124601606785,-3.23082252347422);
//        sampleLatLngs.put(54.0982507554424,-3.25509877557481);
//        sampleLatLngs.put(53.9438823289713,-2.86314157500171);
//        sampleLatLngs.put(54.4370379721279,-3.03667815889833);
//        sampleLatLngs.put(54.0441573088366,-2.89199039371563);
//        sampleLatLngs.put(54.0627670471203,-2.83500239637988);
//        sampleLatLngs.put(54.0663871969417,-2.86202901288172);
//        sampleLatLngs.put(52.3028913646233,-3.5121660282987);
//        sampleLatLngs.put(52.7453470581437,-1.2478766553049);
//        sampleLatLngs.put(52.7308530172529,-1.11223985720479);
//        sampleLatLngs.put(52.7356879784434,-1.09773096158425);
//        sampleLatLngs.put(52.6914904510807,-0.757435428056029);
//        sampleLatLngs.put(52.5919585214306,-0.599443332816379);
//        sampleLatLngs.put(52.4844501544893,-1.15723265615118);
//        sampleLatLngs.put(52.5715499629358,-1.09552537743016);
//        sampleLatLngs.put(52.5693987134393,-1.2108460769383);
//        sampleLatLngs.put(52.6290825152706,-1.17508269222674);
//        sampleLatLngs.put(52.6509694852238,-1.15542496101971);
//        sampleLatLngs.put(52.6824093823332,-1.13452344872001);
//        sampleLatLngs.put(52.6750437252942,-1.18485147000226);
//        sampleLatLngs.put(52.567866902135,-1.32837298601789);
//        sampleLatLngs.put(53.0752148080579,-3.02621995055557);
//        sampleLatLngs.put(53.0561058991441,-2.98442137163687);
//        sampleLatLngs.put(52.9441203870158,-3.12167687970784);
//        sampleLatLngs.put(53.2879431080673,-3.58884279472179);
//        sampleLatLngs.put(52.9107910358665,-3.60056577844009);
//        sampleLatLngs.put(53.296419270357,-3.73098601990586);
//        sampleLatLngs.put(53.2485893849864,-3.96430365151246);
//        sampleLatLngs.put(52.59870422059,-4.04083442913505);
//        sampleLatLngs.put(53.2461585553629,-4.16033191706557);
//        sampleLatLngs.put(53.2007532727336,-4.41462897924388);
//        sampleLatLngs.put(53.2523566323866,-0.549864824434055);
//        sampleLatLngs.put(53.2477380593816,-0.550035972110216);
//        sampleLatLngs.put(53.2456738427461,-0.533410277081068);
//        sampleLatLngs.put(53.2849622640192,-0.293789982251424);
//        sampleLatLngs.put(53.7824119641302,-1.57015788078987);
//        sampleLatLngs.put(53.7755168900332,-1.55261150800331);
//        sampleLatLngs.put(53.7793793687556,-1.6003115830954);
//        sampleLatLngs.put(53.7978009155112,-1.6417650824741);
//        sampleLatLngs.put(53.8472942419197,-1.57970811563488);
//        sampleLatLngs.put(53.9017470447415,-1.68364374763199);
//        sampleLatLngs.put(53.7724416676928,-1.37187592359364);
//        sampleLatLngs.put(53.7493072873724,-1.45282001998545);
//        sampleLatLngs.put(53.7442066207828,-1.59159320520154);
//        sampleLatLngs.put(53.7426530726662,-1.60000797284705);
//        sampleLatLngs.put(53.8086126718556,-1.67297070255416);
//        sampleLatLngs.put(53.7894826574991,-1.67497119426774);
//        sampleLatLngs.put(53.9142821074279,-1.74689608794476);
//        sampleLatLngs.put(53.8147268394425,-1.50120822806553);
//        sampleLatLngs.put(53.8250104503558,-1.50805915031674);
//        sampleLatLngs.put(53.8117179577963,-1.51934672244826);
//        sampleLatLngs.put(53.7985514185081,-1.51289297771524);
//        sampleLatLngs.put(51.8805919143153,-0.43136057607832);
//        sampleLatLngs.put(51.8867659546629,-0.373501934170654);
//        sampleLatLngs.put(51.9088123707263,-0.439827164485722);
//        sampleLatLngs.put(51.8863911375634,-0.429981716840908);
//        sampleLatLngs.put(51.8981122110013,-0.713411643859011);
//        sampleLatLngs.put(51.9163144693783,-0.667672367728626);
//        sampleLatLngs.put(53.4677907543714,-2.15588573048188);
//        sampleLatLngs.put(53.4256329091357,-2.21980908698403);
//        sampleLatLngs.put(53.439737243838,-2.27547571821147);
//        sampleLatLngs.put(53.4058385672399,-2.2607410866652);
//        sampleLatLngs.put(53.4003417865807,-2.30448867141821);
//        sampleLatLngs.put(53.527086025309,-2.25575367384731);
//        sampleLatLngs.put(53.5811302119623,-2.34611522204904);
//        sampleLatLngs.put(53.5036408473743,-2.42933878734769);
//        sampleLatLngs.put(53.451093952778,-2.29623875253455);
//        sampleLatLngs.put(53.4443383289566,-2.29834464447045);
//        sampleLatLngs.put(53.4990784877301,-2.1760200100064);
//        sampleLatLngs.put(53.5187880098024,-2.18911741280034);
//        sampleLatLngs.put(53.4557165211272,-2.34548539362632);
//        sampleLatLngs.put(53.4256525952936,-2.4377921894404);
//        sampleLatLngs.put(53.5199608097973,-2.48200550274465);
//        sampleLatLngs.put(53.5341128112797,-2.47195213085317);
//        sampleLatLngs.put(53.5124952606513,-2.23966627829868);
//        sampleLatLngs.put(51.3805153913462,0.507936481351432);
//        sampleLatLngs.put(51.3466526886374,0.458408273913052);
//        sampleLatLngs.put(51.3455406722421,0.714747957331367);
//        sampleLatLngs.put(51.3384175946006,0.760893281013373);
//        sampleLatLngs.put(51.2891200267469,0.534349234826701);
//        sampleLatLngs.put(51.2716019483562,0.530947517539639);
//        sampleLatLngs.put(51.2660366723628,0.556102302503747);
//        sampleLatLngs.put(51.3936806615111,0.473737954476926);
//        sampleLatLngs.put(51.4151543126245,0.45848700327073);
//        sampleLatLngs.put(51.3565870287131,0.538244569792439);
//        sampleLatLngs.put(51.3408367250925,0.508310954154415);
//        sampleLatLngs.put(51.3492967339014,0.572698074431011);
//        sampleLatLngs.put(52.0650098549753,-0.756295418478949);
//        sampleLatLngs.put(51.9183396619192,-0.829418936884905);
//        sampleLatLngs.put(52.0059063547181,-0.743991853443806);
//        sampleLatLngs.put(52.1573848939719,-0.460651319783617);
//        sampleLatLngs.put(52.1156285992335,-0.465420012698725);
//        sampleLatLngs.put(52.1383650391554,-0.706705856209543);
//        sampleLatLngs.put(52.0312224211694,-0.805964011764966);
//        sampleLatLngs.put(51.5317609055587,-0.098869476260216);
//        sampleLatLngs.put(51.6074799919881,-0.145676944984589);
//        sampleLatLngs.put(51.6065373081777,-0.12201670455517);
//        sampleLatLngs.put(51.6267145792382,-0.133189893556281);
//        sampleLatLngs.put(51.5789821089627,-0.173133735990386);
//        sampleLatLngs.put(51.597415724811,-0.113960795708062);
//        sampleLatLngs.put(51.5575811931749,-0.091286749639077);
//        sampleLatLngs.put(51.5819210599355,-0.118398528435583);
//        sampleLatLngs.put(51.6197886757106,-0.062053767146727);
//        sampleLatLngs.put(54.9877314770968,-1.60681310042648);
//        sampleLatLngs.put(55.1358815999219,-1.60269276223761);
//        sampleLatLngs.put(55.0826255348117,-1.58249624132625);
//        sampleLatLngs.put(55.1254441764099,-1.50779871991086);
//        sampleLatLngs.put(55.1293907464055,-1.52177073488592);
//        sampleLatLngs.put(55.0319444675937,-1.51033712282494);
//        sampleLatLngs.put(55.0120034232333,-1.4612605535098);
//        sampleLatLngs.put(55.01494040942,-1.42936601401107);
//        sampleLatLngs.put(54.8964866639821,-1.53842548073419);
//        sampleLatLngs.put(54.8947690033249,-1.50830649926121);
//        sampleLatLngs.put(54.9722220106828,-1.76347646944614);
//        sampleLatLngs.put(54.9614788919314,-2.13235707875552);
//        sampleLatLngs.put(54.9262134077898,-2.03047433147873);
//        sampleLatLngs.put(54.9915549852806,-1.68912516333458);
//        sampleLatLngs.put(55.1612088222782,-1.69126098479591);
//        sampleLatLngs.put(55.3483300403728,-1.61246016028978);
//        sampleLatLngs.put(55.325133632027,-1.73979080753505);
//        sampleLatLngs.put(55.0010787490261,-1.56531833186198);
//        sampleLatLngs.put(52.8922821990216,-1.00024572891378);
//        sampleLatLngs.put(53.0372593018325,-1.08424627178186);
//        sampleLatLngs.put(53.010082216434,-1.28756432697464);
//        sampleLatLngs.put(53.0588085410054,-1.33652247473559);
//        sampleLatLngs.put(53.1341589290954,-1.24210788358785);
//        sampleLatLngs.put(53.1397090069714,-1.17502339024068);
//        sampleLatLngs.put(53.1587935760176,-1.21774509237141);
//        sampleLatLngs.put(52.9524433722809,-1.12603311352662);
//        sampleLatLngs.put(53.1671075263725,-0.734392029847922);
//        sampleLatLngs.put(53.1482169322853,-0.750229062496691);
//        sampleLatLngs.put(53.0695898179431,-0.803177819578176);
//        sampleLatLngs.put(52.9723521665184,-1.13918276874081);
//        sampleLatLngs.put(52.9724973209792,-1.12948558993631);
//        sampleLatLngs.put(52.9164939010962,-0.625285895878836);
//        sampleLatLngs.put(52.8945550341962,-0.337423646206472);
//        sampleLatLngs.put(53.0033140358658,-0.36099766094453);
//        sampleLatLngs.put(52.9577389637519,-1.09018425091766);
//        sampleLatLngs.put(52.9794280059313,-1.09100969307492);
//        sampleLatLngs.put(53.005819034443,-1.10376381022565);
//        sampleLatLngs.put(52.9537890486787,-1.17990437220941);
//        sampleLatLngs.put(52.9582198736475,-1.17586077487176);
//        sampleLatLngs.put(52.930948880667,-1.2230480482736);
//        sampleLatLngs.put(52.2802123035686,-0.592449043898265);
//        sampleLatLngs.put(52.3026273778266,-0.6008315092004);
//        sampleLatLngs.put(52.1227062500604,-0.990764638279751);
//        sampleLatLngs.put(52.4425087447731,-0.818637505013669);
//        sampleLatLngs.put(52.400898303689,-0.530521583914663);
//        sampleLatLngs.put(51.5963842335618,-3.07278076791128);
//        sampleLatLngs.put(51.6098461639353,-3.09790862806446);
//        sampleLatLngs.put(51.6427594938555,-2.6745735721544);
//        sampleLatLngs.put(51.6339948084052,-2.67736214811865);
//        sampleLatLngs.put(51.6737528192853,-2.77778289076112);
//        sampleLatLngs.put(51.6144707083064,-2.9689151519823);
//        sampleLatLngs.put(51.7713615677418,-3.29077130712182);
//        sampleLatLngs.put(51.7974544691998,-3.19825699277224);
//        sampleLatLngs.put(51.699717899189,-3.04359183090162);
//        sampleLatLngs.put(51.7266909772206,-3.07170799762247);
//        sampleLatLngs.put(52.7581861606385,1.3392637738485);
//        sampleLatLngs.put(52.7920119874782,1.25450268582578);
//        sampleLatLngs.put(52.6657073061825,1.37121261470325);
//        sampleLatLngs.put(52.6755404431025,1.41005679064138);
//        sampleLatLngs.put(52.635246170655,1.26791722914822);
//        sampleLatLngs.put(52.9374861716184,1.24140676385167);
//        sampleLatLngs.put(52.6073108885942,1.73281198799651);
//        sampleLatLngs.put(52.4662146296433,1.7420127749332);
//        sampleLatLngs.put(52.4493898387146,1.69845085699088);
//        sampleLatLngs.put(52.4187133421107,1.66772260657539);
//        sampleLatLngs.put(52.663031698116,1.31177864523734);
//        sampleLatLngs.put(52.6299077725087,1.35712071355254);
//        sampleLatLngs.put(52.6531445597703,1.32184132248319);
//        sampleLatLngs.put(52.6840902550234,1.21136354256456);
//        sampleLatLngs.put(51.543119712265,-0.267948514233389);
//        sampleLatLngs.put(51.5508763283878,-0.265259596730876);
//        sampleLatLngs.put(51.5349562873845,-0.23248560674643);
//        sampleLatLngs.put(51.5610575942588,-0.249045833332994);
//        sampleLatLngs.put(51.5427028935936,-0.171154792917106);
//        sampleLatLngs.put(51.5480573697173,-0.142786366688817);
//        sampleLatLngs.put(51.5348263652633,-0.168327212834737);
//        sampleLatLngs.put(51.5870414047088,-0.255423450168659);
//        sampleLatLngs.put(51.6050804559858,-0.245807618929043);
//        sampleLatLngs.put(53.5922662276564,-2.15238597934387);
//        sampleLatLngs.put(53.6265378337644,-2.21178080270279);
//        sampleLatLngs.put(53.6295390422649,-2.22694737216608);
//        sampleLatLngs.put(53.6923625466629,-2.21444356180933);
//        sampleLatLngs.put(53.6151901502496,-2.15635350343012);
//        sampleLatLngs.put(53.5609489585548,-2.11391227254773);
//        sampleLatLngs.put(53.5809509122793,-2.11074902930652);
//        sampleLatLngs.put(53.581883124464,-2.09267169314121);
//        sampleLatLngs.put(53.5466482422193,-2.08130590492546);
//        sampleLatLngs.put(53.5359875153141,-2.04958573592111);
//        sampleLatLngs.put(53.5301403474557,-2.05988248544015);
//        sampleLatLngs.put(53.5029279718136,-2.06831676353604);
//        sampleLatLngs.put(51.5882973332174,-1.42430477855868);
//        sampleLatLngs.put(51.6656766942186,-1.28506948700914);
//        sampleLatLngs.put(51.7697103950412,-1.58822383284103);
//        sampleLatLngs.put(51.7529466169311,-1.27630462201944);
//        sampleLatLngs.put(51.9799084665321,-1.24063712648325);
//        sampleLatLngs.put(51.7678951431526,-1.48568481799129);
//        sampleLatLngs.put(51.8230777300448,-1.39402668452603);
//        sampleLatLngs.put(51.7686059325341,-1.23182537865201);
//        sampleLatLngs.put(51.8684374746128,-1.47444544029675);
//        sampleLatLngs.put(51.7500027387151,-0.983069342532827);
//        sampleLatLngs.put(53.0054736253396,-0.026179861150192);
//        sampleLatLngs.put(52.38144928383,-0.005774629365132);
//        sampleLatLngs.put(52.3264379494894,-0.239337758223452);
//        sampleLatLngs.put(52.8750918284668,0.497526648316451);
//        sampleLatLngs.put(52.6704692892424,0.38430807385698);
//        sampleLatLngs.put(52.6083219386674,0.381889936765169);
//        sampleLatLngs.put(52.6106374328,0.381661121651379);
//        sampleLatLngs.put(52.4800331070758,-0.461754547224463);
//        sampleLatLngs.put(50.3679295906189,-4.17243368262095);
//        sampleLatLngs.put(50.5761354325166,-4.13122418060564);
//        sampleLatLngs.put(50.4904428695242,-4.11227896504387);
//        sampleLatLngs.put(50.3379959064915,-4.74735840243392);
//        sampleLatLngs.put(50.3831209062831,-4.79542083480544);
//        sampleLatLngs.put(50.3783530026929,-4.13413132759572);
//        sampleLatLngs.put(50.3757410405244,-4.12499859538281);
//        sampleLatLngs.put(50.4032178083909,-4.11864240936211);
//        sampleLatLngs.put(50.3886993864902,-4.05305496810769);
//        sampleLatLngs.put(50.8000548427463,-1.07009204349342);
//        sampleLatLngs.put(50.860253646862,-0.935505464636842);
//        sampleLatLngs.put(50.8032133570227,-1.1395235397155);
//        sampleLatLngs.put(50.846213772934,-1.19329120231727);
//        sampleLatLngs.put(50.8865165408028,-1.25241809268372);
//        sampleLatLngs.put(50.8528038637051,-1.1840145914563);
//        sampleLatLngs.put(50.7325310505635,-0.778026185825254);
//        sampleLatLngs.put(50.7811383042372,-0.903840849517173);
//        sampleLatLngs.put(50.7721410507787,-0.874113010315451);
//        sampleLatLngs.put(50.7827943306638,-0.698328561371362);
//        sampleLatLngs.put(50.7949856198684,-0.613735015496592);
//        sampleLatLngs.put(50.7977573402367,-0.626055076894568);
//        sampleLatLngs.put(50.7887815348383,-0.634419114487007);
//        sampleLatLngs.put(50.6401396338564,-1.37414826471193);
//        sampleLatLngs.put(50.7097491459138,-1.39207292916261);
//        sampleLatLngs.put(50.7269407948349,-1.17498108784487);
//        sampleLatLngs.put(50.8416830706433,-1.06231985293117);
//        sampleLatLngs.put(50.8514702566731,-1.0660729064081);
//        sampleLatLngs.put(50.8542846436154,-1.09805279065633);
//        sampleLatLngs.put(50.8683418571523,-1.0330932697699);
//        sampleLatLngs.put(50.8627179843336,-0.987086149370642);
//        sampleLatLngs.put(50.8549470000313,-0.973558931707987);
//        sampleLatLngs.put(50.8734632642302,-0.98800452404861);
//        sampleLatLngs.put(53.7573972807393,-2.68471836746467);
//        sampleLatLngs.put(53.6919150890877,-2.74248918033229);
//        sampleLatLngs.put(53.6938417959636,-2.73614719479769);
//        sampleLatLngs.put(53.7838623709947,-2.87603177862768);
//        sampleLatLngs.put(53.7225078767731,-2.66343452148387);
//        sampleLatLngs.put(53.6187945823678,-3.00684216435572);
//        sampleLatLngs.put(51.447010884832,-0.965508187426107);
//        sampleLatLngs.put(51.4525817599711,-0.952890263098916);
//        sampleLatLngs.put(51.4101095933537,-0.722222213524886);
//        sampleLatLngs.put(51.4009200715971,-1.44823632902973);
//        sampleLatLngs.put(51.4403858168991,-1.29267735397902);
//        sampleLatLngs.put(51.3545488214606,-1.31976263773156);
//        sampleLatLngs.put(51.2772793641853,-1.07875727123847);
//        sampleLatLngs.put(51.2505413722615,-1.13717843775612);
//        sampleLatLngs.put(51.4430622717095,-1.01336797138373);
//        sampleLatLngs.put(51.4391221955721,-1.0591186031234);
//        sampleLatLngs.put(51.467731326731,-0.959770646990644);
//        sampleLatLngs.put(51.3954711191082,-0.850831839700263);
//        sampleLatLngs.put(51.4319338346625,-0.887204254565294);
//        sampleLatLngs.put(51.3697524956001,-0.801324290401176);
//        sampleLatLngs.put(51.4482354864095,-0.933577010501271);
//        sampleLatLngs.put(51.3883771872871,-1.00952858581264);
//        sampleLatLngs.put(51.5418601086366,-0.910279629595742);
//        sampleLatLngs.put(51.535732815102,-0.897348779945504);
//        sampleLatLngs.put(51.5021284132377,-0.925231363473225);
//        sampleLatLngs.put(51.2365798632921,-0.16168895169869);
//        sampleLatLngs.put(51.2338314868801,-0.103297166192871);
//        sampleLatLngs.put(51.0926874298862,-0.200674931611235);
//        sampleLatLngs.put(51.0667862211148,-0.342279125331103);
//        sampleLatLngs.put(51.0714323385406,-0.365679386680856);
//        sampleLatLngs.put(51.0084495788565,-0.088029926799455);
//        sampleLatLngs.put(51.00959656329,-0.142965485658115);
//        sampleLatLngs.put(51.12525450234,-0.012613012300486);
//        sampleLatLngs.put(51.1138446326166,-0.009587859718647);
//        sampleLatLngs.put(51.233453548343,-0.334305618711515);
//        sampleLatLngs.put(51.5098822791594,0.282742243386205);
//        sampleLatLngs.put(51.5233676898788,0.298149984974816);
//        sampleLatLngs.put(51.5916820661648,0.234649275925796);
//        sampleLatLngs.put(51.589509548465,0.158056170660374);
//        sampleLatLngs.put(51.5316941687994,0.13894114615221);
//        sampleLatLngs.put(53.3362708860725,-1.37441042256877);
//        sampleLatLngs.put(53.3292315052557,-1.34733600183889);
//        sampleLatLngs.put(53.3960888236183,-1.4625774238293);
//        sampleLatLngs.put(53.4622824810934,-1.49167031696074);
//        sampleLatLngs.put(53.1708639669476,-1.42588583063098);
//        sampleLatLngs.put(53.4211542244509,-1.48203384434605);
//        sampleLatLngs.put(53.4971114250303,-1.28113273054921);
//        sampleLatLngs.put(53.353539709943,-1.48581314102802);
//        sampleLatLngs.put(53.5932635836055,-1.4114757416351);
//        sampleLatLngs.put(53.5034341448935,-1.40810667179611);
//        sampleLatLngs.put(53.3424054907111,-1.48438498168705);
//        sampleLatLngs.put(53.3116692978226,-1.15434719170189);
//        sampleLatLngs.put(53.3151161340753,-1.12131628489154);
//        sampleLatLngs.put(53.3928272546861,-1.41502502168351);
//        sampleLatLngs.put(51.6586933761638,-3.80168020803873);
//        sampleLatLngs.put(51.7459986254668,-3.62403703970508);
//        sampleLatLngs.put(51.6185299542196,-3.81128374104374);
//        sampleLatLngs.put(51.6536260361725,-3.60228626027458);
//        sampleLatLngs.put(51.6759355237364,-4.17669084463678);
//        sampleLatLngs.put(51.7878921456406,-3.88035188773682);
//        sampleLatLngs.put(51.9166446288072,-4.00323478294768);
//        sampleLatLngs.put(52.0482062074972,-3.74077582201731);
//        sampleLatLngs.put(51.5670722210208,-3.98755264526077);
//        sampleLatLngs.put(51.9738850704636,-4.35729943808523);
//        sampleLatLngs.put(51.6687323960594,-4.0508307781304);
//        sampleLatLngs.put(51.666089397416,-4.04666232634569);
//        sampleLatLngs.put(51.6720010597642,-3.99578869791282);
//        sampleLatLngs.put(52.0172305089173,-4.83664365450291);
//        sampleLatLngs.put(52.1875216994912,-4.4065395967249);
//        sampleLatLngs.put(52.1783726184264,-4.27352198756272);
//        sampleLatLngs.put(52.068212329338,-4.02177340984985);
//        sampleLatLngs.put(51.6525876705188,-3.97015716985895);
//        sampleLatLngs.put(51.6917134377729,-4.95318683667855);
//        sampleLatLngs.put(51.7193441078253,-5.02449370028606);
//        sampleLatLngs.put(51.4974250315069,-0.077765803250473);
//        sampleLatLngs.put(51.466595346929,-0.056313766428143);
//        sampleLatLngs.put(51.4952432964343,-0.042100645619936);
//        sampleLatLngs.put(51.4897731010228,0.097925995274487);
//        sampleLatLngs.put(51.4883642834075,0.082621218236045);
//        sampleLatLngs.put(51.4101842930557,-0.077587680816116);
//        sampleLatLngs.put(51.4176500793411,-0.060778233454188);
//        sampleLatLngs.put(51.4568921111774,-0.080318424606587);
//        sampleLatLngs.put(51.4643998538108,-0.099568510675245);
//        sampleLatLngs.put(51.4687250383212,-0.031962333104017);
//        sampleLatLngs.put(51.44980304788,-0.029206867863928);
//        sampleLatLngs.put(51.477396130271,-0.083115457523193);
//        sampleLatLngs.put(51.4348916829073,0.04437703853139);
//        sampleLatLngs.put(52.0380299562949,-0.30885205809434);
//        sampleLatLngs.put(52.034875559377,-0.302089816089902);
//        sampleLatLngs.put(52.0849684039898,-0.195627706161479);
//        sampleLatLngs.put(51.9122334900643,-0.175604066484648);
//        sampleLatLngs.put(51.8973105133366,-0.167734955875925);
//        sampleLatLngs.put(51.9400032981828,-0.285874103325481);
//        sampleLatLngs.put(52.0931366172004,0.009765389832127);
//        sampleLatLngs.put(53.2643529873669,-2.14754265322628);
//        sampleLatLngs.put(53.2515190265167,-2.12194448866263);
//        sampleLatLngs.put(53.2607255937764,-2.15145775284879);
//        sampleLatLngs.put(53.3434241960913,-2.12624744330698);
//        sampleLatLngs.put(53.4392645687419,-1.93437253393288);
//        sampleLatLngs.put(53.4011484638376,-2.13496141094956);
//        sampleLatLngs.put(53.3720657419361,-1.99076247066018);
//        sampleLatLngs.put(53.4113875903987,-2.20375903672174);
//        sampleLatLngs.put(53.4240678867225,-2.18892195083516);
//        sampleLatLngs.put(53.4156845524716,-2.18703417122769);
//        sampleLatLngs.put(53.4040824383485,-2.04845374646312);
//        sampleLatLngs.put(53.3681086912096,-2.15523545326031);
//        sampleLatLngs.put(53.3834526070456,-2.12426131412153);
//        sampleLatLngs.put(53.3728790907152,-2.23390597797032);
//        sampleLatLngs.put(53.3356765028654,-2.23317656414891);
//        sampleLatLngs.put(51.517787640978,-0.531572718838576);
//        sampleLatLngs.put(51.4781206547477,-0.506436637499199);
//        sampleLatLngs.put(51.4978208350469,-0.542039677763789);
//        sampleLatLngs.put(51.3912266430051,-0.62685280723558);
//        sampleLatLngs.put(51.5159045489084,-0.706163950927129);
//        sampleLatLngs.put(51.5292537186416,-0.702556389082982);
//        sampleLatLngs.put(51.5788001987291,-0.765180255142778);
//        sampleLatLngs.put(51.3702405766286,-0.188896334134199);
//        sampleLatLngs.put(51.3536085859587,-1.99436463340171);
//        sampleLatLngs.put(51.275199960895,-1.98716290175063);
//        sampleLatLngs.put(51.6158667268218,-1.96309394833858);
//        sampleLatLngs.put(51.5802528979336,-1.80726215249488);
//        sampleLatLngs.put(51.5663984364605,-1.80801315817689);
//        sampleLatLngs.put(51.602944092549,-1.80012013556807);
//        sampleLatLngs.put(51.5483341709563,-1.72610319782198);
//        sampleLatLngs.put(51.5354768505382,-1.90295166132838);
//        sampleLatLngs.put(51.5510011367368,-1.8226011172651);
//        sampleLatLngs.put(50.9126760349519,-1.42202207116528);
//        sampleLatLngs.put(50.9417410820439,-1.38531058598165);
//        sampleLatLngs.put(50.9941159427724,-1.31626602966906);
//        sampleLatLngs.put(51.0743951826312,-1.3518534177134);
//        sampleLatLngs.put(51.0570587057411,-1.32183195091539);
//        sampleLatLngs.put(51.050798840425,-1.16464819808613);
//        sampleLatLngs.put(51.0904460980904,-1.1004075630203);
//        sampleLatLngs.put(50.8614972152704,-1.31667215735866);
//        sampleLatLngs.put(50.8567352532761,-1.28140822499432);
//        sampleLatLngs.put(50.953599931663,-1.20595567252929);
//        sampleLatLngs.put(50.9154386069714,-1.50253013749374);
//        sampleLatLngs.put(50.7445115655746,-1.58895638608007);
//        sampleLatLngs.put(51.0702184508198,-1.80992765164729);
//        sampleLatLngs.put(51.2309409283351,-1.94937924911485);
//        sampleLatLngs.put(51.0637704491476,-2.08184688228486);
//        sampleLatLngs.put(51.1972907210377,-1.77275406417149);
//        sampleLatLngs.put(50.9880170318242,-2.16850653633595);
//        sampleLatLngs.put(54.9048597770383,-1.36807276602569);
//        sampleLatLngs.put(54.8417505210211,-1.35161893158804);
//        sampleLatLngs.put(51.546822509591,0.688953811816497);
//        sampleLatLngs.put(51.5375177322193,0.720767254780166);
//        sampleLatLngs.put(51.5507518133121,0.712425569277848);
//        sampleLatLngs.put(51.6044753841906,0.66631968810882);
//        sampleLatLngs.put(51.5930001150645,0.603429609447089);
//        sampleLatLngs.put(51.5531949357495,0.599380974210691);
//        sampleLatLngs.put(51.5632826275354,0.577100134069713);
//        sampleLatLngs.put(51.5189460112518,0.570083680194615);
//        sampleLatLngs.put(51.5626063506254,0.649035370412152);
//        sampleLatLngs.put(53.021280993855,-2.19261148633085);
//        sampleLatLngs.put(53.0305522200612,-2.16002772259966);
//        sampleLatLngs.put(52.8986916874838,-2.13648415775909);
//        sampleLatLngs.put(52.8177746596103,-2.12332098928515);
//        sampleLatLngs.put(52.7939500083748,-2.0854520709449);
//        sampleLatLngs.put(52.786495776153,-2.08810634515568);
//        sampleLatLngs.put(52.830880926354,-2.08228908761808);
//        sampleLatLngs.put(53.0552336988898,-2.14079861543721);
//        sampleLatLngs.put(52.9732159672745,-2.16246613510353);
//        sampleLatLngs.put(52.9802863359286,-2.12926503906312);
//        sampleLatLngs.put(52.9786028639402,-2.09365076409399);
//        sampleLatLngs.put(53.0072834264014,-2.19350287259179);
//        sampleLatLngs.put(53.04508498777,-2.19761015535759);
//        sampleLatLngs.put(53.046234588069,-2.20871318547778);
//        sampleLatLngs.put(53.1204396545804,-2.16236411630515);
//        sampleLatLngs.put(51.4816779205516,-0.188402388756977);
//        sampleLatLngs.put(51.4271221751461,-0.259147393714126);
//        sampleLatLngs.put(51.4271619491742,-0.129797559347917);
//        sampleLatLngs.put(51.4244153101465,-0.113842669144556);
//        sampleLatLngs.put(51.4136761413132,-0.128305913060266);
//        sampleLatLngs.put(51.4223377710108,-0.180379469306189);
//        sampleLatLngs.put(51.4192592491577,-0.168060977327537);
//        sampleLatLngs.put(51.4297199009886,-0.206584528761525);
//        sampleLatLngs.put(51.4470754536086,-0.120478815832156);
//        sampleLatLngs.put(51.4849155927373,-0.167591327047875);
//        sampleLatLngs.put(51.4620133711115,-0.149952564921987);
//        sampleLatLngs.put(51.4916523149968,-0.183282131591605);
//        sampleLatLngs.put(52.7330774802739,-2.71502550591794);
//        sampleLatLngs.put(52.914528952419,-3.0687268813508);
//        sampleLatLngs.put(52.899274354198,-2.70659476024828);
//        sampleLatLngs.put(52.4435206825797,-3.5405343555162);
//        sampleLatLngs.put(52.7078908662424,-2.73804355565437);
//        sampleLatLngs.put(52.6478319860015,-3.3251844317159);
//        sampleLatLngs.put(52.4113562719693,-4.06843066685027);
//        sampleLatLngs.put(52.3647510891477,-2.87594024853632);
//        sampleLatLngs.put(51.0178712885002,-3.09951899371927);
//        sampleLatLngs.put(50.9727016055383,-2.76907256329224);
//        sampleLatLngs.put(51.0310060460006,-3.08351548657771);
//        sampleLatLngs.put(50.8661821606839,-2.96219753836403);
//        sampleLatLngs.put(51.1764393688789,-3.32468380527041);
//        sampleLatLngs.put(51.076536779324,-2.92985508716336);
//        sampleLatLngs.put(51.2469787375844,-2.99402308836738);
//        sampleLatLngs.put(51.2402902107599,-2.99700180567696);
//        sampleLatLngs.put(51.2401024430092,-2.98305923954754);
//        sampleLatLngs.put(52.6959094986318,-2.48284705976101);
//        sampleLatLngs.put(52.6502284469849,-2.43764467099003);
//        sampleLatLngs.put(52.9212948545388,-2.36456450853664);
//        sampleLatLngs.put(50.9532522208556,0.720522823765996);
//        sampleLatLngs.put(50.8919205553889,0.441533852766233);
//        sampleLatLngs.put(50.8749021659555,0.553783913204572);
//        sampleLatLngs.put(50.8821334801046,0.550497032782321);
//        sampleLatLngs.put(50.8560050606309,0.548033557370774);
//        sampleLatLngs.put(50.8379632606304,0.473629367796723);
//        sampleLatLngs.put(50.8478313843696,0.471011702963173);
//        sampleLatLngs.put(50.4554855987691,-3.55522080471509);
//        sampleLatLngs.put(50.3737239353966,-3.54098525328222);
//        sampleLatLngs.put(50.3503812581693,-3.58210330171215);
//        sampleLatLngs.put(50.2782035885673,-3.85859046087001);
//        sampleLatLngs.put(50.377554974591,-3.68307627582769);
//        sampleLatLngs.put(50.0526437275317,-5.24816331885595);
//        sampleLatLngs.put(50.1156399646902,-5.55327569742083);
//        sampleLatLngs.put(50.1161609543632,-5.45263238099975);
//        sampleLatLngs.put(50.2154912450427,-5.47822750399463);
//        sampleLatLngs.put(50.1777671213219,-5.44421550541152);
//        sampleLatLngs.put(54.5727066329158,-1.23644579158437);
//        sampleLatLngs.put(54.6148443723004,-1.05400496622525);
//        sampleLatLngs.put(54.5642256500748,-0.983786766991235);
//        sampleLatLngs.put(54.5508041617962,-0.848914845577993);
//        sampleLatLngs.put(54.5759282387853,-1.36303745431996);
//        sampleLatLngs.put(54.582398846318,-1.31053193427887);
//        sampleLatLngs.put(54.6224449307666,-1.28118780160708);
//        sampleLatLngs.put(54.6733002337665,-1.23967758656092);
//        sampleLatLngs.put(54.6769985714284,-1.22061077256683);
//        sampleLatLngs.put(54.7483806620955,-1.28930651277039);
//        sampleLatLngs.put(54.7459414970213,-1.29327946591048);
//        sampleLatLngs.put(54.7192907146019,-1.41188334429704);
//        sampleLatLngs.put(54.7022110754365,-1.42587908643976);
//        sampleLatLngs.put(54.5704749345846,-1.22047822382587);
//        sampleLatLngs.put(54.5514777233221,-1.26082171855244);
//        sampleLatLngs.put(54.5539911313949,-1.24721644417282);
//        sampleLatLngs.put(54.5353932258392,-1.24088223309833);
//        sampleLatLngs.put(54.5529540533307,-1.15414309072887);
//        sampleLatLngs.put(54.5667917791138,-1.14994367966528);
//        sampleLatLngs.put(54.5315695916936,-1.2079915247225);
//        sampleLatLngs.put(54.4274645132662,-1.07904179585546);
//        sampleLatLngs.put(51.4543289797155,-0.306113984148664);
//        sampleLatLngs.put(51.4262649612949,-0.358029198014955);
//        sampleLatLngs.put(51.4409482865141,-0.385129296014093);
//        sampleLatLngs.put(51.4756223754587,-0.361124703033291);
//        sampleLatLngs.put(51.4910177049051,-0.299885743953477);
//        sampleLatLngs.put(51.478004345275,-0.286199408386824);
//        sampleLatLngs.put(51.4735558332485,-0.281470373274136);
//        sampleLatLngs.put(51.4988155748184,-0.42761707545381);
//        sampleLatLngs.put(51.5196039303713,-0.208619646781509);
//        sampleLatLngs.put(51.5080607256358,-0.217675358569806);
//        sampleLatLngs.put(51.5034777550954,-0.217292233980289);
//        sampleLatLngs.put(51.5097065387145,-0.313154056658477);
//        sampleLatLngs.put(51.4983570884022,-0.212305446965532);
//        sampleLatLngs.put(51.5166120313177,-0.162283875915711);
//        sampleLatLngs.put(51.5099640101908,-0.139306491608614);
//        sampleLatLngs.put(51.5020073884634,-0.271654508163585);
//        sampleLatLngs.put(51.4989164266006,-0.196565090068458);
//        sampleLatLngs.put(53.4538617375712,-2.76127909636351);
//        sampleLatLngs.put(53.4538625249347,-2.76537520260868);
//        sampleLatLngs.put(53.4708269588095,-2.71262105548099);
//        sampleLatLngs.put(53.3692042302914,-2.34137293452387);
//        sampleLatLngs.put(53.3833074360907,-2.55447254685662);
//        sampleLatLngs.put(53.397848139846,-2.66905009741298);
//        sampleLatLngs.put(53.2612798105408,-2.67558090878663);
//        sampleLatLngs.put(53.326209597424,-2.68764191162934);
//        sampleLatLngs.put(53.3693719989638,-2.77035203577769);
//        sampleLatLngs.put(53.4175344980706,-2.71687526038605);
//        sampleLatLngs.put(51.5250371988097,-0.115756574645047);
//        sampleLatLngs.put(51.6340919033871,-0.340622500899072);
//        sampleLatLngs.put(51.6507354028787,-0.378538033962735);
//        sampleLatLngs.put(51.6534227697864,-0.356454432122937);
//        sampleLatLngs.put(51.6337614687739,-0.468515027798586);
//        sampleLatLngs.put(51.6467114725265,-0.459695512606402);
//        sampleLatLngs.put(53.6907266005311,-1.68699823616524);
//        sampleLatLngs.put(53.6564098411162,-1.48849171723161);
//        sampleLatLngs.put(53.6606684159368,-1.56295282022378);
//        sampleLatLngs.put(53.7098330466865,-1.43386403997675);
//        sampleLatLngs.put(53.6692719836621,-1.3494659644938);
//        sampleLatLngs.put(53.6919071987554,-1.29740078414894);
//        sampleLatLngs.put(53.5899663963326,-1.32173831683358);
//        sampleLatLngs.put(53.5522163727139,-2.6331751360881);
//        sampleLatLngs.put(53.5693199639128,-2.59133186241473);
//        sampleLatLngs.put(53.5285446443221,-2.56987009383485);
//        sampleLatLngs.put(53.5321950994543,-2.66371379792623);
//        sampleLatLngs.put(53.5816202594445,-2.65275195463069);
//        sampleLatLngs.put(53.5072858863159,-2.51877224160828);
//        sampleLatLngs.put(53.5667710881561,-2.77476628717823);
//        sampleLatLngs.put(52.1843086796788,-2.22126454502535);
//        sampleLatLngs.put(52.1853906397349,-2.21960248651082);
//        sampleLatLngs.put(52.0954109623242,-1.90634276370167);
//        sampleLatLngs.put(52.1148637793276,-2.34864280861773);
//        sampleLatLngs.put(52.2131086833624,-2.19359990432413);
//        sampleLatLngs.put(52.5711491828803,-1.96416198396872);
//        sampleLatLngs.put(52.5671892831765,-1.98657542133746);
//        sampleLatLngs.put(52.6897352582723,-2.05256668484733);
//        sampleLatLngs.put(52.6913464838236,-2.04767145283354);
//        sampleLatLngs.put(52.689943495166,-1.81422551702231);
//        sampleLatLngs.put(52.59354737039,-1.98637561782342);
//        sampleLatLngs.put(52.6009721314175,-1.98057086035092);
//        sampleLatLngs.put(52.6189704514978,-1.95174539907581);
//        sampleLatLngs.put(52.6679634934211,-2.01922487014592);
//        sampleLatLngs.put(52.6693556243538,-2.02582078862811);
//        sampleLatLngs.put(52.5869566175858,-2.13986603661405);
//        sampleLatLngs.put(52.6149335826517,-2.13135958822005);
//        sampleLatLngs.put(52.4635685996638,-2.3399566796832);
//        sampleLatLngs.put(52.5734432234363,-2.12536267205214);
//        sampleLatLngs.put(52.6321503041593,-2.20082483604166);
//        sampleLatLngs.put(53.9541047070242,-1.06007521694851);
//        sampleLatLngs.put(54.4804044472784,-0.610991588437624);
//        sampleLatLngs.put(53.9359731890717,-1.13263854928308);
//        sampleLatLngs.put(53.9555184294989,-1.10274463017037);
//        sampleLatLngs.put(54.0010742276019,-0.442127504347869);
//        sampleLatLngs.put(53.9536474662606,-1.145712505206);
//        sampleLatLngs.put(53.9577296934841,-1.24246538497583);
//        sampleLatLngs.put(54.0401036976045,-1.24388548272754);
//        sampleLatLngs.put(53.8862615495158,-0.854071557446893);
//        sampleLatLngs.put(53.8181469136024,-0.613844459913173);
//        sampleLatLngs.put(53.7720038664653,-1.06244774641677);
    }



}