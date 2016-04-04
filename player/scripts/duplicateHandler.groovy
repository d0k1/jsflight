import org.openqa.selenium.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONArray;


/**
 * Базовый скрипт определения дублей событий, нужен для корректного ввода текстом плеером, и обработки кликов в дерево
 * Скрипту доступны след. переменные:
 * 		current - текущее событие
 * 		previous - прыдущее событие
 * 		logger - лог
 */

def getTarget(def event){
	if (event.has("target2"))
        	{
            		return event.getString("target2");
	}
        if (!event.has("target1"))
        {
            return "";
        }
        JSONArray array = event.getJSONArray("target1");
        if (array.isNull(0))
        {
            return "";
        }

        String target = array.getJSONObject(0).getString("getxp");
        return target;
}

def nonDuplicateEvents = ['keyup', 'keydown','keypress','mousewheel'];

def duplicates = ['click', 'mousedown'];
if(current['target2'].equals(previous['target2']) && current.type in duplicates && previous.type in duplicates){
	return true;
}

if(current.type.equals('mousedown') && previous.type.equals('mousedown') && current['target2'].contains('aria-posinset') && previous['target2'].contains('aria-posinset')) {
	return false;
}

if(current.type in nonDuplicateEvents && previous.type in nonDuplicateEvents){
	return false;
}

return previous.type.equals(current.type) && previous.url.equals(current.url) && getTarget(current).equals(getTarget(previous)) ;
