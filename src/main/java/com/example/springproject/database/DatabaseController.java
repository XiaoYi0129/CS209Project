package com.example.springproject.database;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import javax.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/jdbc")
public class DatabaseController {
//  @Resource
//  private JdbcTemplate jdbcTemplate;
//
//  @RequestMapping("/test")
//  public List<Map<String, Object>> getDbType(){
//    String sql = "select * from release_test";
//    List<Map<String, Object>> list =  jdbcTemplate.queryForList(sql);
//    for (Map<String, Object> map : list) {
//      Set<Entry<String, Object>> entries = map.entrySet( );
//      if(entries != null) {
//        Iterator<Entry<String, Object>> iterator = entries.iterator( );
//        while(iterator.hasNext( )) {
//          Entry<String, Object> entry = iterator.next( );
//          Object key = entry.getKey( );
//          Object value = entry.getValue();
//          System.out.println(key+":"+value);
//        }
//      }
//    }
//    return list;
//  }


}
