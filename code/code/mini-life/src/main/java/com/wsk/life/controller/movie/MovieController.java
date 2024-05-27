package com.wsk.life.controller.movie;

import com.alibaba.fastjson.JSON;
import com.wsk.life.bean.AllInformation;
import com.wsk.life.bean.MovieBean;
import com.wsk.life.bean.OneSubject;
import com.wsk.life.bean.celebrity.CelebrityBean;
import com.wsk.life.bean.celebrity.USbox;
import com.wsk.life.bean.maoyan.Hot;
import com.wsk.life.bean.maoyan.cinema.Cinemas;
import com.wsk.life.bean.maoyan.cinemas.Cinema;
import com.wsk.life.bean.maoyan.movie.MovieInformation;
import com.wsk.life.controller.AsideController;
import com.wsk.life.pojo.UserInformation;
import com.wsk.life.redis.RedisKey;
import com.wsk.life.tool.*;
import com.wsk.life.tool.http.HttpUtil;
import com.wsk.life.tool.http.HttpsRequest;
import org.apache.http.HttpResponse;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.servlet.http.HttpServletRequest;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by wsk1103 on 2017/10/22.
 */
@Controller
public class MovieController extends AsideController {
    private static final String ENCODE = "UTF-8";
    private static final String GET = "GET";
    private static final String POST = "POST";
    private static final String URL = "http://op.juhe.cn/onebox/movie/video";
    private static final String KEY = "e712295ae7ca460ec31624dd3dfe2094";
    private static final String DOUBAN_URL = "https://api.douban.com";

    //模糊查询电影信息
    @RequestMapping(value = "search/movie/result")
    public String searchMovieResult(Model model, HttpServletRequest request, @ModelAttribute("name") String q) {
        UserInformation userInformation = JwtUtil.getLoginUser();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        if (Tool.getInstance().isNullOrEmpty(q)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/search";
        Map<String, String> map = new HashMap<>();
        map.put("q", q);
        AllInformation information = getMovieInformation(url, map, ENCODE, GET);
        model.addAttribute("action", 3);
        model.addAttribute("movie", information);
        model.addAttribute("movie_name", q + "搜索结果");
        getUserCounts(model, userInformation.getId());
        model.addAttribute("myFriends", getMyFriends(JwtUtil.getLoginUser().getId()));
        return "soonList";
    }

    //查看电影信息
    @RequestMapping(value = "search/movie/information")
    public String searchMovie(Model model, HttpServletRequest request, @RequestParam String id) {
        UserInformation userInformation = JwtUtil.getLoginUser();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/subject/" + id;
//        System.out.println(url);
        Map map = new HashMap();
        String name = "";
        try {
            String result = HttpUtils.submitPostData(url, map, ENCODE, GET);
//            System.out.println(result);
            OneSubject subject = JSON.parseObject(result, OneSubject.class);
            String down = "ftp://www.wsk1103.cc:8088/down/" + id + "/" + subject.getTitle() + ".mkv";
            model.addAttribute("down", down);
            model.addAttribute("subject", subject);
            name = subject.getTitle();
        } catch (Exception e) {
            e.printStackTrace();
            model.addAttribute("result", "error");
        }
        MovieBean movieBean;
        Map<String, String> params = new HashMap<>();
        params.put("key", KEY);
        params.put("q", name);
        params.put("dtype", "json");
        try {
//            String sr = HttpUtils.submitPostData(URL, params, ENCODE, POST);
            String sr = POSTtoJSON.getInstance().post(URL, params, POST);
            movieBean = JSON.parseObject(sr, MovieBean.class);
        } catch (Exception e) {
            e.printStackTrace();
            movieBean = new MovieBean();
        }
        model.addAttribute("movie", movieBean);
        model.addAttribute("result", "success");
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        model.addAttribute("myFriends", getMyFriends(JwtUtil.getLoginUser().getId()));
        return "information/movieInformation";
    }

    //查看影人条目信息
    @RequestMapping(value = "celebrity/{id}")
    public String celebrity(@PathVariable String id, Model model, HttpServletRequest request) {
        UserInformation userInformation = JwtUtil.getLoginUser();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/celebrity/" + id;
//        System.out.println(url);
        Map<String, String> params = new HashMap<>();
        String result = HttpUtils.submitPostData(url, params, ENCODE, GET);
//        System.out.println(result);
        model.addAttribute("result", "success");
        CelebrityBean celebrityBean;
        try {
            celebrityBean = JSON.parseObject(result, CelebrityBean.class);
        } catch (Exception e) {
            e.printStackTrace();
            celebrityBean = new CelebrityBean();
            model.addAttribute("result", "error");
        }
        model.addAttribute("celebrity", celebrityBean);
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        model.addAttribute("myFriends", getMyFriends(JwtUtil.getLoginUser().getId()));
        return "movie/celebrity";
    }
    /**
     * 通用模板
     * */
    public String commonTemplate(String doubanUrl, String movieName, Model model, HttpServletRequest request) {
        UserInformation userInformation = JwtUtil.getLoginUser();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String movieHtml = null;
        try {
            HttpsRequest httpsRequest = new HttpsRequest();
            String redisKey = RedisKey.getMovieKey(doubanUrl);
            movieHtml = redisUtils.get(redisKey);
            if(StringUtils.isEmpty(movieHtml)){
                movieHtml = httpsRequest.httpsRequest(doubanUrl,"GET","UTF-8",null);
                redisUtils.set(redisKey,movieHtml,3600);
            }
        }
        catch (Exception ex){
            model.addAttribute("movieHtml", "网络不稳定，请稍后再试！");
        }
        model.addAttribute("movie_name", movieName);
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        model.addAttribute("myFriends", getMyFriends(JwtUtil.getLoginUser().getId()));
        return  movieHtml;
    }
    /*
    * 获取image的src列表
    * */
    public static List<String> getImageList(String htmlStr) {
        List<String> pics = new ArrayList<>();
        if(StringUtils.isEmpty(htmlStr)){
            return  pics;
        }
        String img = "";
        Pattern p_image;
        Matcher m_image;
        String regEx_img = "<img.*src\\s*=\\s*(.*?)[^>]*?>";
        p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            // 得到<img />数据
            img = m_image.group();
            // 匹配<img>中的src数据
            Matcher m = Pattern.compile("src\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }
    /*
     * 获取详情url
     * */
    public static List<String> getDetailLink(String htmlStr) {
        List<String> pics = new ArrayList<>();
        if(StringUtils.isEmpty(htmlStr)){
            return  pics;
        }
        String img = "";
        Pattern p_image;
        Matcher m_image;
        String regEx_img = "<a.*href\\s*=\\s*(.*?)[^>]*?>";
        p_image = Pattern.compile(regEx_img, Pattern.CASE_INSENSITIVE);
        m_image = p_image.matcher(htmlStr);
        while (m_image.find()) {
            // 得到<img />数据
            img = m_image.group();
            // 匹配<img>中的src数据
            Matcher m = Pattern.compile("href\\s*=\\s*\"?(.*?)(\"|>|\\s+)").matcher(img);
            while (m.find()) {
                pics.add(m.group(1));
            }
        }
        return pics;
    }
    public String changeHtmlImage(String content) throws Exception{
        List<String> listUrl = getImageList(content);
        for (String url : listUrl) {
            String imagePath = HttpUtil.downloadPicture(url,"image/movies");
            content = content.replace(url,imagePath);
        }
        return content;
    }
    public String changeDetailLink(String content) throws Exception{
        List<String> listUrl = getDetailLink(content);
        for (String url : listUrl) {
            String urlEncode = Base64.getEncoder().encodeToString(url.getBytes());
            //影片詳情
            if(url.contains("subject")){
                content = content.replace(url,String.format("/movie/detail/%s",urlEncode));
            }
//            else if(url.contains("trailer")){
//                content = content.replace(url,String.format("/movie/trailer/%s",urlEncode));
//            }
        }
        return content;
    }
    /**
     * 即将上映的电影
     * */
    @RequestMapping(value = "coming/soon")
    public String comingSoon(Model model, HttpServletRequest request) throws  Exception{
        String url = "https://movie.douban.com/cinema/later/guangzhou";
        String movieHtml = commonTemplate(url,"即将上映影讯",model,request);
        Document htmlDoc = Jsoup.parse(movieHtml);
        if(htmlDoc != null){
            movieHtml = DocumentUtil.getElementStringBySelect(htmlDoc,".tab-bd",null,0,null);
            movieHtml = changeHtmlImage(movieHtml);
            movieHtml = changeDetailLink(movieHtml);
            model.addAttribute("movieHtml", movieHtml);
        }
        else if(StringUtils.isEmpty(movieHtml) == false){
            return movieHtml;
        }
        return "movie/soonList";
    }

    //top250
    @RequestMapping(value = "top")
    public String top(Model model, HttpServletRequest request) {
        UserInformation userInformation = JwtUtil.getLoginUser();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/top250";
        HashMap<String, String> params = new HashMap<>();
        AllInformation allInformation = getMovieInformation(url, params, ENCODE, GET);
        model.addAttribute("movie", allInformation);
        model.addAttribute("movie_name", "高分电影");
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        model.addAttribute("myFriends", getMyFriends(JwtUtil.getLoginUser().getId()));
        return "movie/soonList";
    }

    /**
     * 正在上映影讯
     * */
    @RequestMapping(value = "coming")
    public String nowPlaying(Model model, HttpServletRequest request) throws Exception {
        String url = "https://movie.douban.com/cinema/nowplaying/guangzhou";
        String movieHtml = commonTemplate(url,"正在上映影讯",model,request);
        Document htmlDoc = Jsoup.parse(movieHtml);
        if(htmlDoc != null){
            movieHtml = DocumentUtil.getElementStringBySelect(htmlDoc,".mod-bd",null,0,null);
            movieHtml = changeHtmlImage(movieHtml);
            movieHtml = changeDetailLink(movieHtml);
            model.addAttribute("movieHtml", movieHtml);
        }
        else if(StringUtils.isEmpty(movieHtml) == false){
            return movieHtml;
        }
        return "movie/nowPlaying";
    }

    //电影的详情，
    @RequestMapping(value = "movie/detail/{urlEncode}")
    public String moviteDetail(@PathVariable String urlEncode, Model model, HttpServletRequest request) throws Exception{
        String url = new String(Base64.getDecoder().decode(urlEncode), StandardCharsets.UTF_8);
        String movieHtml = commonTemplate(url,"影片的详情，",model,request);
        Document htmlDoc = Jsoup.parse(movieHtml);
        if(htmlDoc != null){
            String movieHtml1 = DocumentUtil.getElementStringBySelect(htmlDoc,".indent",null,0,null);
            String movieHtml2 = DocumentUtil.getElementStringBySelect(htmlDoc,".related-info",null,0,null);
            movieHtml1 = changeHtmlImage(movieHtml1);
            movieHtml2 = changeHtmlImage(movieHtml2);
            String title =  DocumentUtil.getElementStringBySelect(htmlDoc,"h1",null,0,null);
            if(StringUtils.isEmpty(title) ==false){
                model.addAttribute("movie_name", title);
            }
            model.addAttribute("movieHtml1", movieHtml1);
            model.addAttribute("movieHtml2", movieHtml2);
        }
        else if(StringUtils.isEmpty(movieHtml) == false){
            return movieHtml;
        }
        return "movie/movieDetail";
    }
    //预告的详情，
    @RequestMapping(value = "movie/trailer/{urlEncode}")
    public String moviteTrailer(@PathVariable String urlEncode, Model model, HttpServletRequest request) throws Exception{
        String url = new String(Base64.getDecoder().decode(urlEncode), StandardCharsets.UTF_8);
        String movieHtml = commonTemplate(url,"影片的详情，",model,request);
        Document htmlDoc = Jsoup.parse(movieHtml);
        if(htmlDoc != null){
            String movieHtml1 = DocumentUtil.getElementStringBySelect(htmlDoc,"#player",null,0,null);
            movieHtml = changeHtmlImage(movieHtml1);
            String title =  DocumentUtil.getElementStringBySelect(htmlDoc,"h1",null,0,null);
            if(StringUtils.isEmpty(title) ==false){
                model.addAttribute("movie_name", title);
            }
            model.addAttribute("movieHtml", movieHtml);
        }
        else if(StringUtils.isEmpty(movieHtml) == false){
            return movieHtml;
        }
        return "movie/movieTrailer";
    }
    //热映电影的详情，
    @RequestMapping(value = "hot/movie/information/{id}")
    public String hotMovieInformation(@PathVariable String id, Model model, HttpServletRequest request) {
        UserInformation userInformation = JwtUtil.getLoginUser();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:../login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = "http://m.maoyan.com/movie/" + id + ".json";
        String result = HttpUtils.maoyan(url);
        MovieInformation information = JSON.parseObject(result, MovieInformation.class);
        model.addAttribute("movie", information);
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        model.addAttribute("myFriends", getMyFriends(JwtUtil.getLoginUser().getId()));
        return "movie/hot_movie_information";
    }

    //北美票房榜
    @RequestMapping(value = "us/box")
    public String usBox(Model model, HttpServletRequest request) {
        UserInformation userInformation = getLoginUser();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:/login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = DOUBAN_URL + "/v2/movie/us_box";
        HashMap<String, String> params = new HashMap<>();
        USbox allInformation = getUsBox(url, params, ENCODE, GET);
        model.addAttribute("movie", allInformation);
        model.addAttribute("movie_name", "北美票房");
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        model.addAttribute("myFriends", getMyFriends(JwtUtil.getLoginUser().getId()));
        return "movie/us_box";
    }

    //附件电影院
    @RequestMapping(value = "cinemas/{id}")
    public String cinemas(Model model, HttpServletRequest request, @PathVariable String id) {
        UserInformation userInformation = JwtUtil.getLoginUser();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:../login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = "http://m.maoyan.com/cinemas.json";
        String result = HttpUtils.maoyan(url);
        Cinema cinema = JSON.parseObject(result, Cinema.class);

        url = "http://m.maoyan.com/movie/" + id + ".json";
        result = HttpUtils.maoyan(url);
        MovieInformation information = JSON.parseObject(result, MovieInformation.class);
        String img = information.getData().getMovieDetailModel().getImg();
        model.addAttribute("img", img);
        model.addAttribute("movie", cinema);
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        model.addAttribute("myFriends", getMyFriends(JwtUtil.getLoginUser().getId()));
        return "movie/cinemas";
    }

    //单个影院
    @RequestMapping(value = "cinema/{id}")
    public String cinema(@PathVariable String id, Model model, HttpServletRequest request) {
        UserInformation userInformation = JwtUtil.getLoginUser();
        if (Tool.getInstance().isNullOrEmpty(userInformation)) {
            return "redirect:../login";
        }
        model.addAttribute("userInformation", userInformation);
        String url = "http://m.maoyan.com/showtime/wrap.json?cinemaid=" + id;
        String result = HttpUtils.maoyan(url);
        Cinemas cinema = JSON.parseObject(result, Cinemas.class);

//        url = "http://m.maoyan.com/movie/"+id+".json";
//        result = HttpUtils.maoyan(url);
//        MovieInformation information = JSON.parseObject(result, MovieInformation.class);
//        String img = information.getData().getMovieDetailModel().getImg();
//        model.addAttribute("img", img);
        model.addAttribute("movie", cinema);
        model.addAttribute("action", 3);
        getUserCounts(model, userInformation.getId());
        model.addAttribute("myFriends", getMyFriends(JwtUtil.getLoginUser().getId()));
        return "movie/cinema";
    }

/*    //获得点赞数量，收藏数量，评论数量
    private void getUserCounts(Model model, int uid) {
        model.addAttribute("comments", commentCriticService.getUserCounts(uid));
        model.addAttribute("critics", publishCriticService.getUserCounts(uid));
        model.addAttribute("goods", goodCriticService.getUserCounts(uid));
        model.addAttribute("collections", collectionCriticService.getUserCounts(uid));
    }*/

    private AllInformation getMovieInformation(String url, Map params, String encode, String action) {
        AllInformation information;
        try {
            String re = HttpUtils.submitPostData(url, params, encode, action);
            System.err.println("豆瓣电影：" + re);
            information = JSON.parseObject(re, AllInformation.class);
        } catch (Exception e) {
            e.printStackTrace();
            information = new AllInformation();
        }
        return information;
    }

    private USbox getUsBox(String url, Map params, String encode, String action) {
        USbox information;
        try {
            information = JSON.parseObject(HttpUtils.submitPostData(url, params, encode, action), USbox.class);
        } catch (Exception e) {
            e.printStackTrace();
            information = new USbox();
        }
        return information;
    }

/*    private void getFriend(Model model, int uid) {
        List<MyFriends> list = myFriendsService.getFid(uid);
        List<MyFriendsBean> ids = new ArrayList<>();
        for (MyFriends myFriends : list) {
            UserInformation userInformation = userInformationService.selectByPrimaryKey(myFriends.getFid());
            MyFriendsBean myFriendsBean = new MyFriendsBean();
            myFriendsBean.setAvatar(userInformation.getAvatar());
            myFriendsBean.setFid(myFriends.getFid());
            myFriendsBean.setId(myFriends.getId());
            myFriendsBean.setUid(myFriends.getUid());
            myFriendsBean.setName(userInformation.getName());
            ids.add(myFriendsBean);
        }
        model.addAttribute("myFriends", ids);
    }*/
}
