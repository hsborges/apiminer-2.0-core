package org.apiminer.util.weaver;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Stack;

import org.apiminer.daos.ApiMethodDAO;
import org.apiminer.daos.DatabaseType;
import org.apiminer.daos.ExampleDAO;
import org.apiminer.daos.GenericDAO;
import org.apiminer.entities.api.ApiElement;
import org.apiminer.entities.api.ApiMethod;
import org.apiminer.entities.example.AssociatedElement;
import org.apiminer.entities.example.Example;
import org.apiminer.entities.example.Recommendation;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

//TODO improve it
public class AndroidWeaverIDE {
	
	public static void instrumentJavaDoc(String inputFile, String outputFile) throws IOException {
		Document jsoup = Jsoup.parse(new File(inputFile),"UTF-8");
		
		String className = null;
		String basePakage = "/reference/";
		if (inputFile.contains(basePakage)){
			className = inputFile.substring(inputFile.indexOf(basePakage)+basePakage.length());
			className = className.replace("/", ".").substring(0, className.length()-5);
		}
		
		StringBuilder scriptContent = new StringBuilder("<script type=\"text/javascript\">").append("\n")
			.append("var method, destiny = String(window.location).split(\"#\");").append("\n")
			.append("if (destiny.lenght > 1) method = destiny[1];").append("\n")
			.append(" else method = '';").append("\n")
			.append(String.format("window.location = \"http://www.apiminer.org/static/docs/%s#\"+method;",inputFile.substring(inputFile.indexOf("/docs-original/")+"/docs-original/".length()))).append("\n")
			.append("</script>");
		
		jsoup.getElementsByTag("head").first().prepend(scriptContent.toString());
		
		if (className != null && className.startsWith("android.")) {
			Elements elements = jsoup.select("a");
			for (Element element : elements) {
				if (element.hasAttr("name") && element.attr("name").contains("(") && element.attr("name").contains(")")) {
					String methodStr = className.concat(".").concat(element.attr("name"));
					Element desc = element.nextElementSibling().getElementsByClass("jd-details-descr").first();
					if (desc != null) {
						ApiMethod method = new ApiMethodDAO().find(1, methodStr, DatabaseType.PRE_PROCESSING);
						if (method != null) {
//							System.out.println("Analisando metodo: "+method.getSimpleFullName());
							List<Example> examples = new ExampleDAO().findByMethod(method.getSimpleFullName());
							if (examples != null && !examples.isEmpty()) {
								String exampleStr = examples.get(0).getFormattedCodeExample();
								desc.appendElement("br");
								desc.appendElement("b").text("Example:");
								desc.appendElement("br");
								desc.appendElement("p").appendElement("pre").text(exampleStr.substring(1, exampleStr.length()-1));
								
								GenericDAO  dao = new GenericDAO() {
									@Override
									public Class<?> getObjectType() {
										return Recommendation.class;
									}
								};
								
								Recommendation combination = (Recommendation) dao.find(method, DatabaseType.EXAMPLES);
								if (combination != null && !combination.getAssociatedElements().isEmpty()) {
									AssociatedElement rs = combination.getAssociatedElements().get(0);
									if (rs.getRecommendedExamples().isEmpty()) {
										Element b = desc.appendElement("b");
										b.text("Example provided by ");
										Element a = b.appendElement("a");
										a.attr("href", "http://www.apiminer.org");
										a.text("APIMiner 2.0.");
										b.appendElement("br");
										continue;
									}
									
									desc.appendElement("br");
									desc.appendElement("b").text("Frequently called with ");
									
									String goTo = "";
									for (int i =0 ; i < inputFile.substring(inputFile.indexOf(basePakage)+basePakage.length(), inputFile.length()-5).split("/").length - 1; i++ ){
										goTo = goTo.concat("../");
									}
										
									Iterator<ApiElement> it = rs.getElements().iterator();
									while(it.hasNext()) {
										ApiElement next = it.next();
										
										if (!(next instanceof ApiMethod))
											continue;
											
										String name = ((ApiMethod) next).getFullName().replace(((ApiMethod) next).getApiClass().getName(), ((ApiMethod) next).getApiClass().getName().replace(".", "/"));
										
										name = name.substring(0, name.substring(0, name.lastIndexOf("(")-1).lastIndexOf("."));
										
										Element a = desc.appendElement("b").appendElement("a");
										a.text(it.hasNext() ? ((ApiMethod) next).getSimpleFullName()+", " : ((ApiMethod) next).getSimpleFullName());
										a.attr("href", goTo.concat(name.concat(".html")));
										
									}
									
									exampleStr = rs.getRecommendedExamples().get(0).getFormattedCodeExample();
									
									desc.appendElement("br");
									desc.appendElement("p").appendElement("pre").text(exampleStr.substring(1, exampleStr.length()-1));
									
									Element b = desc.appendElement("b");
									b.text("Examples provided by ");
									Element a = b.appendElement("a");
									a.attr("href", "http://www.apiminer.org");
									a.text("APIMiner 2.0.");
									b.appendElement("br");
									
								} else {
								
									Element b = desc.appendElement("b");
									b.text("Example provided by ");
									Element a = b.appendElement("a");
									a.attr("href", "http://www.apiminer.org");
									a.text("APIMiner 2.0.");
									b.appendElement("br");
								
								}
								
							}
						}else{
							System.err.println("Metodo nao encontrado: "+methodStr);
						}
					}
				}
			}
		}
		
		FileOutputStream fos = new FileOutputStream(outputFile);
		fos.write(jsoup.toString().getBytes());
		fos.close();
		
	}

	public static void main(String args[]) throws IOException{

		String input = "/home/hudson/Documents/APIMINER2/docs/docs-original/";
		String output = "/home/hudson/Documents/APIMINER2/docs/docs-ide-instrumented/";

		Stack<File> files = new Stack<File>();
		files.add(new File(input));
		do{
			File inputFile = files.pop();
			if (inputFile.isDirectory()) {
				files.addAll(Arrays.asList(inputFile.listFiles()));
			}else{
				File outputFile = new File(output, inputFile.getAbsolutePath().replaceFirst(input, ""));
				if (!outputFile.getParentFile().exists()) {
					outputFile.getParentFile().mkdirs();
				}
				if (inputFile.getName().toLowerCase().endsWith(".html")) {
					instrumentJavaDoc(inputFile.getAbsolutePath(), outputFile.getAbsolutePath());
				}else{
					copyFiles(inputFile, outputFile);
				}
			}
		}while(!files.isEmpty());


	}

	public static void copyFiles(String sourceFile, String destFile) throws IOException {
		copyFiles(new File(sourceFile), new File(destFile));
	}

	public static void copyFiles(File sourceFile, File destFile) throws IOException {
		FileInputStream fis = new FileInputStream(sourceFile);
		FileOutputStream fos = new FileOutputStream(destFile);

		byte[] buffer = new byte[1024];
		int length;
		while ((length = fis.read(buffer)) > 0){
			fos.write(buffer, 0, length);
		}

		fis.close();
		fos.flush();
		fos.close();
	}

}
