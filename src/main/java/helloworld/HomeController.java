package helloworld;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.app.ApplicationInstanceInfo;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class HomeController
{
	@Autowired(required = false)
	DataSource dataSource;

	@Autowired
	ApplicationInstanceInfo instanceInfo;

	@RequestMapping(value = "/", method = RequestMethod.GET)
	public String home(Model model)
	{
		Map<Class<?>, String> services = new LinkedHashMap<Class<?>, String>();
		
		if (dataSource != null)
		{
			services.put(dataSource.getClass(), toString(dataSource));
		}
				
		
		model.addAttribute("services", services.entrySet());

		model.addAttribute("instanceInfo", instanceInfo);

		return "home";
	}

	private String toString(DataSource dataSource)
	{
		if (dataSource == null)
		{
			return "<none>";
		}
		else
		{
			try
			{
				Field urlField = ReflectionUtils.findField(dataSource.getClass(), "url");
				ReflectionUtils.makeAccessible(urlField);
				return stripCredentials((String) urlField.get(dataSource));
			}
			catch (Exception fe)
			{
				try
				{
					Method urlMethod = ReflectionUtils.findMethod(dataSource.getClass(), "getUrl");
					ReflectionUtils.makeAccessible(urlMethod);
					return stripCredentials((String) urlMethod.invoke(dataSource, (Object[]) null));
				}
				catch (Exception me)
				{
					return "<unknown> " + dataSource.getClass();
				}
			}
		}
	}

	private String stripCredentials(String urlString)
	{
		try
		{
			if (urlString.startsWith("jdbc:"))
			{
				urlString = urlString.substring("jdbc:".length());
			}
			URI url = new URI(urlString);
			return new URI(url.getScheme(), null, url.getHost(), url.getPort(), url.getPath(), null, null).toString();
		}
		catch (URISyntaxException e)
		{
			System.out.println(e);
			return "<bad url> " + urlString;
		}
	}

}