package com.codex.phoenixmiles;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

final class AirportCatalog {
    private static final Map<String, Airport> AIRPORTS = new HashMap<>();
    private static final Map<String, String> ALIASES = new HashMap<>();
    private static final Map<String, String> CITY_DEFAULTS = new HashMap<>();
    private static final Map<String, List<Airport>> CITY_AIRPORTS = new HashMap<>();
    private static final String AIRPORT_DATA =
            "PEK|北京|首都|40.077349|116.596702\n" +
            "PKX|北京|大兴|39.501289|116.413967\n" +
            "CAN|广州|白云|23.392401|113.299004\n" +
            "SZX|深圳|宝安|22.639474|113.803262\n" +
            "WUH|武汉|天河|30.774798|114.213723\n" +
            "CGO|郑州|新郑|34.526497|113.849165\n" +
            "XIY|西安|咸阳|34.442207|108.762385\n" +
            "KMG|昆明|长水|25.110313|102.936743\n" +
            "HGH|杭州|萧山|30.236090|120.428865\n" +
            "NKG|南京|禄口|31.735032|118.865949\n" +
            "PVG|上海|浦东|31.143400|121.805000\n" +
            "TAO|青岛|胶东|36.361953|120.088171\n" +
            "CTU|成都|双流|30.558257|103.945966\n" +
            "CKG|重庆|江北|29.712254|106.651895\n" +
            "TFU|成都|天府|30.312520|104.441284\n" +
            "HKG|香港|赤鱲角|22.311840|113.914862\n" +
            "TPE|台北|桃园|25.077700|121.233002\n" +
            "DSN|鄂尔多斯|伊金霍洛|39.493514|109.859900\n" +
            "HET|呼和浩特|白塔|40.849658|111.824598\n" +
            "SJW|石家庄|正定|38.280701|114.696999\n" +
            "TSN|天津|滨海|39.124401|117.346001\n" +
            "TYN|太原|武宿|37.746899|112.627998\n" +
            "BHY|北海|福成|21.538659|109.293683\n" +
            "CSX|长沙|黄花|28.189199|113.220001\n" +
            "KWL|桂林|两江|25.219828|110.039553\n" +
            "NNG|南宁|吴圩|22.598071|108.181922\n" +
            "SWA|揭阳|潮汕|23.552000|116.503300\n" +
            "ZUH|珠海|金湾|22.006399|113.375999\n" +
            "ZHA|湛江|吴川|21.481667|110.590278\n" +
            "HAK|海口|美兰|19.934900|110.459000\n" +
            "BAR|琼海|博鳌|19.140951|110.452766\n" +
            "SYX|三亚|凤凰|18.302900|109.412003\n" +
            "LHW|兰州|中川|36.515202|103.620003\n" +
            "INC|银川|河东|38.322758|106.393214\n" +
            "XNN|西宁|曹家堡|36.527750|102.040215\n" +
            "SHA|上海|虹桥|31.198104|121.334260\n" +
            "XMN|厦门|高崎|24.543889|118.127454\n" +
            "CZX|常州|奔牛|31.920485|119.775460\n" +
            "KHN|南昌|昌北|28.864815|115.902710\n" +
            "FOC|福州|长乐|25.929254|119.672524\n" +
            "TNA|济南|遥墙|36.857201|117.216003\n" +
            "NGB|宁波|栎社|29.826700|121.461998\n" +
            "NTG|南通|兴东|32.073566|120.980076\n" +
            "HFE|合肥|新桥|31.987790|116.976900\n" +
            "WUX|无锡|硕放|31.496952|120.430380\n" +
            "WNZ|温州|龙湾|27.910572|120.853465\n" +
            "XUZ|徐州|观音|34.059056|117.555278\n" +
            "YTY|扬州|泰州|32.563400|119.719800\n" +
            "YNT|烟台|蓬莱|37.659724|120.978124\n" +
            "KWE|贵阳|龙洞堡|26.541805|106.804020\n" +
            "LXA|拉萨|贡嘎|29.298001|90.911951\n" +
            "URC|乌鲁木齐|地窝堡|43.913584|87.479372\n" +
            "KHG|喀什|喀什|39.542273|76.020230\n" +
            "TLQ|吐鲁番|交河|43.030800|89.098700\n" +
            "CGQ|长春|龙嘉|43.996201|125.684998\n" +
            "DDG|丹东|浪头|40.025453|124.286690\n" +
            "HRB|哈尔滨|太平|45.623402|126.250000\n" +
            "DLC|大连|周水子|38.965719|121.538477\n" +
            "SHE|沈阳|桃仙|41.639800|123.483668\n" +
            "MFM|澳门|氹仔|22.149599|113.592003\n" +
            "KHH|高雄|小港|22.577101|120.349998\n" +
            "BPE|秦皇岛|北戴河|39.666384|119.061384\n" +
            "HLD|呼伦贝尔|东山（海拉尔东山）|49.208616|119.822301\n" +
            "NZH|满洲里|西郊|49.566667|117.330000\n" +
            "BAV|包头|东河|40.560001|109.997002\n" +
            "YCU|运城|张孝|35.117823|111.034023\n" +
            "ZQZ|张家口|宁远|40.738664|114.933395\n" +
            "CGD|常德|桃花源|28.918900|111.639999\n" +
            "DYG|张家界|荷花|29.104749|110.442786\n" +
            "LZH|柳州|白莲|24.207500|109.390999\n" +
            "LYA|洛阳|北郊|34.741100|112.388000\n" +
            "NNY|南阳|姜营|32.982696|112.617467\n" +
            "XFN|襄阳|刘集|32.152222|112.291666\n" +
            "YIH|宜昌|三峡|30.554132|111.482563\n" +
            "DNH|敦煌|莫高|40.161953|94.812827\n" +
            "GOQ|格尔木|格尔木|36.400600|94.786102\n" +
            "JGN|嘉峪关|嘉峪关|39.859052|98.339344\n" +
            "UYN|榆林|榆阳|38.359710|109.590927\n" +
            "JHG|西双版纳|嘎洒|21.974648|100.762224\n" +
            "LJG|丽江|三义|26.677483|100.244944\n" +
            "DOY|东营|胜利|37.501370|118.789863\n" +
            "KOW|赣州|黄金|25.853333|114.778889\n" +
            "LYG|连云港|花果山|34.414060|119.178990\n" +
            "LYI|临沂|启阳|35.052918|118.411828\n" +
            "JJN|泉州|晋江|24.795855|118.588599\n" +
            "HIA|淮安|涟水|33.792712|119.126657\n" +
            "TXN|黄山|屯溪|29.733299|118.255997\n" +
            "WEF|潍坊|南苑|36.646702|119.119003\n" +
            "WEH|威海|大水泊|37.187099|122.228996\n" +
            "YIW|义乌|义乌|29.342095|120.031160\n" +
            "HSN|舟山|普陀山|29.933874|122.362307\n" +
            "NGQ|阿里|昆莎|32.097940|80.053971\n" +
            "BPX|昌都|邦达|30.553600|97.108299\n" +
            "JZH|九寨|黄龙|32.853333|103.682222\n" +
            "MIG|绵阳|南郊|31.428101|104.740997\n" +
            "TEN|铜仁|凤凰|27.883333|109.308889\n" +
            "XIC|西昌|青山|27.989100|102.183998\n" +
            "ACX|兴义|万峰林|25.083423|104.960804\n" +
            "KRL|库尔勒|梨城|41.614979|86.140817\n" +
            "KRY|克拉玛依|古海|45.466550|84.952700\n" +
            "HTN|和田|和田|37.038502|79.864899\n" +
            "KNH|金门|（尚义）|24.427900|118.359001\n" +
            "TTT|台东|（丰年）|22.754856|121.101794\n" +
            "CYI|嘉义|（水上）|23.462577|120.390544\n" +
            "RMQ|台中|清泉岗|24.264700|120.621002\n" +
            "TNN|台南|台南|22.950399|120.206001\n" +
            "MZG|澎湖|马公|23.568701|119.627998\n" +
            "HUN|花莲|花莲|24.023163|121.617991\n" +
            "CDE|承德|普宁|41.122500|118.073889\n" +
            "CIF|赤峰|玉龙|42.159723|118.840971\n" +
            "CIH|长治|王村|36.247501|113.125999\n" +
            "DAT|大同|云冈|40.061390|113.480509\n" +
            "ERL|二连浩特|赛乌素|43.424079|112.091081\n" +
            "YIE|阿尔山|伊尔施|47.310600|119.911700\n" +
            "HDG|邯郸|邯郸|36.524824|114.424126\n" +
            "HUO|霍林郭勒|霍林河|45.487222|119.407222\n" +
            "LFQ|临汾|尧都|36.132629|111.641236\n" +
            "LLV|吕梁|大武|37.683333|111.142778\n" +
            "TVS|唐山|三女河|39.717800|118.002625\n" +
            "TGO|通辽|通辽|43.556702|122.199997\n" +
            "UCB|乌兰察布|集宁|41.130266|113.107274\n" +
            "WUA|乌海|乌海|39.793400|106.799300\n" +
            "HLH|乌兰浩特|义勒利特|46.195333|122.008333\n" +
            "XIL|锡林浩特|锡林浩特|43.915600|115.963997\n" +
            "WUT|忻州|五台山|38.597456|112.969173\n" +
            "RLK|巴彦淖尔|天吉泰|40.926358|107.740930\n" +
            "NZL|扎兰屯|成吉思汗|47.865942|122.768662\n" +
            "AEB|百色|巴马|23.720600|106.959999\n" +
            "HJJ|怀化|芷江|27.443087|109.704666\n" +
            "HCZ|郴州|北湖|25.753419|112.845404\n" +
            "FUO|佛山|沙堤|23.082500|113.070833\n" +
            "HCJ|河池|金城江|24.804344|107.710819\n" +
            "HNY|衡阳|南岳|26.722080|112.617958\n" +
            "HUZ|惠州|平潭|23.049999|114.599998\n" +
            "LLF|永州|零陵|26.338661|111.610043\n" +
            "MXZ|梅州|梅县|24.263425|116.097857\n" +
            "HSC|韶关|丹霞|24.978600|113.420998\n" +
            "WGN|邵阳|武冈|26.806123|110.641042\n" +
            "WUZ|梧州|西江|23.403160|111.093310\n" +
            "YLX|玉林|福绵|22.433042|110.119996\n" +
            "YYA|岳阳|三荷|29.311699|113.281574\n" +
            "ENH|恩施|许家坪|30.320299|109.485001\n" +
            "SHS|荆州|沙市|30.292810|112.448540\n" +
            "HPG|神农架|红坪|31.626000|110.340000\n" +
            "WDS|十堰|武当山|32.592889|110.906296\n" +
            "XAI|信阳|明港|32.540819|114.079141\n" +
            "XYI|三沙|永兴|16.833000|112.344000\n" +
            "AKA|安康|富强|32.756960|108.873380\n" +
            "HXD|海西|德令哈|37.125286|97.268658\n" +
            "GMQ|果洛|玛沁|34.418066|100.301144\n" +
            "GYU|固原|六盘山|36.078889|106.216944\n" +
            "HBQ|海北|祁连|38.008068|100.645065\n" +
            "HTT|海西|花土沟|38.201645|90.837843\n" +
            "HZG|汉中|城固|33.133527|107.203817\n" +
            "JIC|金昌|金川|38.542222|102.348333\n" +
            "LNL|陇南|成县|33.789918|105.790014\n" +
            "IQN|庆阳|西峰|35.802638|107.598896\n" +
            "GXH|甘南|夏河|34.819014|102.622261\n" +
            "ENY|延安|南泥湾|36.479413|109.464083\n" +
            "YUS|玉树|巴塘|32.836389|97.036389\n" +
            "ZHY|中卫|沙坡头|37.573125|105.154454\n" +
            "YZY|张掖|甘州|38.801899|100.675003\n" +
            "CWJ|沧源|佤山|23.276331|99.373169\n" +
            "DLU|大理|荒草坝|25.649401|100.319000\n" +
            "LNJ|临沧|博尚|23.738100|100.025002\n" +
            "SYM|普洱|思茅|22.793301|100.959000\n" +
            "AQG|安庆|天柱山|30.582199|117.050003\n" +
            "FUG|阜阳|西关|32.882157|115.734364\n" +
            "JGS|吉安|井冈山（三都）|26.856899|114.737000\n" +
            "HZA|菏泽|牡丹|35.212972|115.736748\n" +
            "JDZ|景德镇|罗家|29.338600|117.176003\n" +
            "JUH|池州|九华山|30.740300|117.685600\n" +
            "JIU|九江|庐山|29.476944|115.801111\n" +
            "JNG|济宁|曲阜|35.647358|116.743269\n" +
            "JUZ|衢州|衢州|28.966130|118.898793\n" +
            "LCX|龙岩|冠豸山|25.675920|116.745907\n" +
            "HYN|台州|路桥|28.562201|121.429001\n" +
            "RIZ|日照|山字河|35.405033|119.324403\n" +
            "SQJ|三明|沙县|26.426300|117.833600\n" +
            "SQD|上饶|三清山|28.379700|117.964300\n" +
            "WHA|芜湖|宣州|31.104500|118.666870\n" +
            "YIC|宜春|明月山|27.802500|114.306200\n" +
            "YNZ|盐城|南洋|33.428317|120.205450\n" +
            "AVA|安顺|黄果树|26.260556|105.873333\n" +
            "BZX|巴中|恩阳|31.738420|106.644872\n" +
            "DAX|达州|河市|31.130200|107.429500\n" +
            "GYS|广元|盘龙|32.390254|105.694571\n" +
            "AHJ|阿坝|红原|32.531540|102.352240\n" +
            "KJH|凯里|黄平|26.972000|107.988000\n" +
            "LLB|荔波|荔波|25.452500|107.961667\n" +
            "LZO|泸州|蓝田|29.030357|105.468407\n" +
            "NAO|南充|高坪|30.798104|106.164150\n" +
            "LZY|林芝|米林|29.303301|94.335297\n" +
            "LPF|六盘水|月照|26.609417|104.979000\n" +
            "JIQ|黔江|武陵山|29.513333|108.831111\n" +
            "RKZ|日喀则|和平|29.350876|89.299157\n" +
            "TCZ|腾冲|驼峰|24.938056|98.485833\n" +
            "CQW|重庆|仙女山|29.465658|107.693664\n" +
            "WSK|重庆|巫山|31.068960|109.708958\n" +
            "WXN|万州|五桥|30.801700|108.433000\n" +
            "YBP|宜宾|五粮液|28.858431|104.526157\n" +
            "ZYI|遵义|新舟|27.810723|107.247189\n" +
            "AKU|阿克苏|温宿|41.262501|80.291702\n" +
            "AAT|阿勒泰|雪都|47.749886|88.085808\n" +
            "BPL|博乐|阿拉山口|44.895461|82.300070\n" +
            "IQM|且末|玉都|38.234516|85.465462\n" +
            "FYN|富蕴|可可托海|46.804169|89.512006\n" +
            "HMI|哈密|伊州|42.841400|93.669197\n" +
            "SHF|石河子|花园|44.242100|85.890500\n" +
            "KCA|库车|龟兹|41.677856|82.872917\n" +
            "KJI|布尔津|喀纳斯|48.222300|86.995900\n" +
            "NLT|新源|那拉提|43.431800|83.378600\n" +
            "RQA|若羌|楼兰|38.974722|88.008333\n" +
            "QSZ|莎车|叶尔羌|38.245420|77.056149\n" +
            "TCG|塔城|千泉|46.672501|83.340797\n" +
            "TWC|图木舒克|唐王城|39.886663|79.233408\n" +
            "YTW|于田|万方|36.808500|81.782700\n" +
            "YIN|伊宁|伊宁|43.955799|81.330299\n" +
            "AOG|鞍山|腾鳌|41.105301|122.853996\n" +
            "DBC|白城|长安|45.505278|123.019722\n" +
            "NBS|白山|长白山|42.066944|127.602222\n" +
            "CHG|朝阳|朝阳|41.538101|120.434998\n" +
            "DQA|大庆|萨尔图|46.750883|125.138642\n" +
            "FYJ|抚远|东极|48.197219|134.362980\n" +
            "HEK|黑河|瑷珲|50.171621|127.308884\n" +
            "JGD|大兴安岭|鄂伦春|50.371389|124.117500\n" +
            "JMU|佳木斯|东郊|46.842793|130.464260\n" +
            "JSJ|建三江|湿地|47.108248|132.657920\n" +
            "JXA|鸡西|兴凯湖|45.293000|131.193000\n" +
            "JNZ|锦州|锦州湾|40.936032|121.277114\n" +
            "LDS|伊春|林都|47.752056|129.019125\n" +
            "MDG|牡丹江|海浪|44.525172|129.568634\n" +
            "OHE|漠河|古莲|52.916871|122.422759\n" +
            "NDG|齐齐哈尔|三家子|47.229969|123.914179\n" +
            "YSQ|松原|查干湖|44.931143|124.552121\n" +
            "TNH|通化|三源浦|42.048435|125.733963\n" +
            "DTU|五大连池|德都|48.441037|126.128374\n" +
            "YNJ|延吉|朝阳川|42.882801|129.451004\n" +
            "YKH|营口|兰旗|40.542524|122.358600\n" +
            "TSA|台北|松山|25.067244|121.552822\n" +
            "AXF|阿拉善左旗|巴彦浩特|38.748317|105.584160\n" +
            "RHT|阿拉善右旗|巴丹吉林|39.225000|101.546000\n" +
            "EJN|额济纳旗|桃来|42.015500|101.000500\n" +
            "THQ|天水|麦积山|34.560100|105.860343\n" +
            "CNI|长海|大长山岛|39.266441|122.666960";

    static {
        loadAirportData();
        addSpecialAliases();
    }

    private AirportCatalog() {
    }

    static Airport byCode(String code) {
        if (code == null) {
            return null;
        }
        return AIRPORTS.get(code.trim().toUpperCase(Locale.US));
    }

    static Airport findFirstIn(String text) {
        List<Airport> airports = findAllIn(text);
        return airports.isEmpty() ? null : airports.get(0);
    }

    static List<Airport> findAllIn(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalized = normalize(text);
        List<Hit> hits = new ArrayList<>();
        for (Map.Entry<String, String> entry : ALIASES.entrySet()) {
            String alias = entry.getKey();
            if (alias.isEmpty()) {
                continue;
            }

            int from = 0;
            int index;
            while ((index = normalized.indexOf(alias, from)) >= 0) {
                hits.add(new Hit(index, alias.length(), AIRPORTS.get(entry.getValue())));
                from = index + alias.length();
            }
        }
        addFuzzyHits(normalized, hits);

        Collections.sort(hits, (left, right) -> {
            if (left.index != right.index) {
                return Integer.compare(left.index, right.index);
            }
            return Integer.compare(right.length, left.length);
        });

        List<Airport> result = new ArrayList<>();
        for (Hit hit : hits) {
            if (hit.airport == null || containsCode(result, hit.airport.code)) {
                continue;
            }
            result.add(hit.airport);
        }
        return result;
    }

    static List<Airport> findCityDefaultsIn(String text) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        String normalized = normalize(text);
        List<Hit> hits = new ArrayList<>();
        for (Map.Entry<String, String> entry : CITY_DEFAULTS.entrySet()) {
            String city = entry.getKey();
            if (city.isEmpty()) {
                continue;
            }

            int from = 0;
            int index;
            while ((index = normalized.indexOf(city, from)) >= 0) {
                hits.add(new Hit(index, city.length(), AIRPORTS.get(entry.getValue())));
                from = index + city.length();
            }
        }

        Collections.sort(hits, (left, right) -> {
            if (left.index != right.index) {
                return Integer.compare(left.index, right.index);
            }
            return Integer.compare(right.length, left.length);
        });

        List<Airport> result = new ArrayList<>();
        for (Hit hit : hits) {
            if (hit.airport == null || containsCode(result, hit.airport.code)) {
                continue;
            }
            result.add(hit.airport);
        }
        return result;
    }

    static List<Airport> cityAlternativesForCode(String code) {
        Airport airport = byCode(code);
        if (airport == null) {
            return Collections.emptyList();
        }
        List<Airport> airports = CITY_AIRPORTS.get(normalize(airport.city));
        if (airports == null || airports.isEmpty()) {
            return Collections.singletonList(airport);
        }
        return new ArrayList<>(airports);
    }

    static String normalize(String value) {
        return value == null ? "" : value
                .toUpperCase(Locale.US)
                .replaceAll("[\s\u00A0\u2007\u202F\u200B\u200C\u200D]+", "")
                .replace("T1", "")
                .replace("T2", "")
                .replace("T3", "")
                .replace("Ｔ１", "")
                .replace("Ｔ２", "")
                .replace("Ｔ３", "")
                .replace("航站楼", "")
                .replace("機場", "机场")
                .replace("机杨", "机场")
                .replace("机扬", "机场")
                .replace("机埸", "机场")
                .replace("机坊", "机场")
                .replace("首部", "首都")
                .replace("苜都", "首都")
                .replace("酋都", "首都")
                .replace("首郡", "首都");
    }

    private static void loadAirportData() {
        for (String row : AIRPORT_DATA.split("\n")) {
            String[] parts = row.split("\|", -1);
            if (parts.length != 5) {
                continue;
            }
            add(new Airport(
                    parts[0],
                    parts[1],
                    parts[2],
                    safeDouble(parts[3]),
                    safeDouble(parts[4])
            ));
        }
    }

    private static void addSpecialAliases() {
        addAliases("PEK",
                "北京首都机场", "北京首都国际机场", "北京首都国际机场T3", "北京首都国际机场T2",
                "北京首都国际", "北京首都国际机", "北京首都国际机杨", "北京首都国际机扬",
                "北京首都机", "北京首都机杨", "北京首都机扬",
                "首都机场", "首都国际机场", "首都国际机场T3", "首都国际机场T2",
                "首都国际", "首都国际机", "首都国际机杨", "首都国际机扬",
                "首都机", "首都机杨", "首都机扬");
        addAliases("PKX", "北京大兴机场", "北京大兴国际机场", "大兴机场", "大兴国际机场");
        addAliases("CKG", "重庆江北机场", "江北机场", "江北国际机场", "江北国际机场T3", "江北国际机场T2");
        addAliases("TFU", "成都天府机场", "天府机场", "天府国际机场");
        addAliases("CTU", "成都双流机场", "双流机场", "双流国际机场");
        addAliases("SHA", "上海虹桥机场", "虹桥机场", "虹桥国际机场");
        addAliases("PVG", "上海浦东机场", "浦东机场", "浦东国际机场");
        addAliases("WUX", "苏南硕放", "苏南硕放机场", "苏南硕放国际机场");
        addAliases("LLB", "黔南荔波", "黔南荔波机场");
        addAliases("HKG", "香港国际机场", "香港赤鱲角", "赤鱲角机场");
        addAliases("MFM", "澳门国际机场", "澳门氹仔", "氹仔机场");
        addAliases("KHH", "高雄小港", "小港机场", "高雄国际机场");
    }

    private static void add(Airport airport, String... aliases) {
        AIRPORTS.put(airport.code, airport);
        ALIASES.put(airport.code, airport.code);
        if (airport.city.equals(airport.name)) {
            ALIASES.put(normalize(airport.city + "机场"), airport.code);
        } else {
            ALIASES.put(normalize(airport.city + airport.name), airport.code);
            ALIASES.put(normalize(airport.city + airport.name + "机场"), airport.code);
            ALIASES.put(normalize(airport.name + "机场"), airport.code);
        }
        addCityDefault(airport.city, airport.code);
        addCityAirport(airport);
        for (String alias : aliases) {
            ALIASES.put(normalize(alias), airport.code);
        }
    }

    private static void addAliases(String airportCode, String... aliases) {
        if (!AIRPORTS.containsKey(airportCode)) {
            return;
        }
        for (String alias : aliases) {
            ALIASES.put(normalize(alias), airportCode);
        }
    }

    private static void addCityDefault(String city, String airportCode) {
        String normalizedCity = normalize(city);
        if (!normalizedCity.isEmpty() && AIRPORTS.containsKey(airportCode) && !CITY_DEFAULTS.containsKey(normalizedCity)) {
            CITY_DEFAULTS.put(normalizedCity, airportCode);
        }
    }

    private static void addCityAirport(Airport airport) {
        String normalizedCity = normalize(airport.city);
        if (normalizedCity.isEmpty()) {
            return;
        }
        List<Airport> airports = CITY_AIRPORTS.get(normalizedCity);
        if (airports == null) {
            airports = new ArrayList<>();
            CITY_AIRPORTS.put(normalizedCity, airports);
        }
        if (!containsCode(airports, airport.code)) {
            airports.add(airport);
        }
    }

    private static boolean containsCode(List<Airport> airports, String code) {
        for (Airport airport : airports) {
            if (airport.code.equals(code)) {
                return true;
            }
        }
        return false;
    }

    private static void addFuzzyHits(String normalized, List<Hit> hits) {
        addFuzzyHit(hits, fuzzyBeijingCapitalIndex(normalized), "PEK");
    }

    private static void addFuzzyHit(List<Hit> hits, int index, String airportCode) {
        if (index < 0) {
            return;
        }
        Airport airport = AIRPORTS.get(airportCode);
        if (airport != null) {
            hits.add(new Hit(index, 2, airport));
        }
    }

    private static int fuzzyBeijingCapitalIndex(String normalized) {
        int direct = firstIndexOfAny(normalized,
                "首都国际", "首都机场", "首都机", "北京首都", "PEK");
        if (direct >= 0) {
            return direct;
        }

        int beijing = normalized.indexOf("北京");
        int capital = normalized.indexOf("首都");
        if (capital >= 0) {
            int nearbyEnd = Math.min(normalized.length(), capital + 10);
            String nearby = normalized.substring(capital, nearbyEnd);
            if (nearby.contains("国际") || nearby.contains("机场") || nearby.contains("机")) {
                return capital;
            }
        }
        if (beijing >= 0 && capital >= 0 && Math.abs(beijing - capital) <= 12) {
            return Math.min(beijing, capital);
        }
        return -1;
    }

    private static int firstIndexOfAny(String value, String... needles) {
        int best = -1;
        for (String needle : needles) {
            int index = value.indexOf(needle);
            if (index >= 0 && (best < 0 || index < best)) {
                best = index;
            }
        }
        return best;
    }

    private static double safeDouble(String value) {
        try {
            return Double.parseDouble(value);
        } catch (RuntimeException ignored) {
            return 0;
        }
    }

    private static final class Hit {
        final int index;
        final int length;
        final Airport airport;

        Hit(int index, int length, Airport airport) {
            this.index = index;
            this.length = length;
            this.airport = airport;
        }
    }
}
