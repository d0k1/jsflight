import org.openqa.selenium.*;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * Базовый скрипт определения дублей событий, нужен для корректного ввода текстом плеером, и обработки кликов в дерево
 * Скрипту доступны след. переменные:
 * 		current - текущее событие
 * 		previous - прыдущее событие
 * 		logger - лог
 */

def nonDuplicateEvents = ['keyup', 'keydown','keypress','mousewheel'];

if(current.type.equals('mousedown') && previous.type.equals('mousedown') && current['target2'].contains('aria-posinset') && current['target2'].contains('aria-posinset')) {
	return false;
}

if(current.type in nonDuplicateEvents && previous.type in nonDuplicateEvents){
	return false;
}

return true;
