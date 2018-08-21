package cp;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import jxl.Sheet;
import jxl.Workbook;
import jxl.read.biff.BiffException;

public class CasosDePrueba {

	WebDriver driver;
	WebDriverWait wait;

	@BeforeMethod
	public void ejecutarNavegador() {
		
		System.setProperty("webdriver.chrome.driver", "chromedriver.exe");
		ChromeOptions op = new ChromeOptions();
		op.addArguments("--start-maximized");
		driver = new ChromeDriver(op);
		wait = new WebDriverWait(driver, 30);
		driver.get("https://www.despegar.com.ar/");
	}

	@Test (priority = 1)
	public void caso1() throws BiffException, IOException {

		// Objetos para manipular el archivo Excel donde estan los parametros
		File fl = new File("Data.xls");
		Workbook wb = Workbook.getWorkbook(fl);
		Sheet sh = wb.getSheet("Caso 1");

		// Parametros
		String origen = sh.getCell(1, 0).getContents();
		String destino = sh.getCell(1, 1).getContents();

		//Obtengo parametros de fecha de salida
		String dia_p = sh.getCell(1, 4).getContents();
		String mes_p = sh.getCell(2, 4).getContents();
		String ano_p = sh.getCell(3, 4).getContents();

		//Obtengo parametros de fecha de regreso
		String dia_r = sh.getCell(1, 5).getContents();
		String mes_r = sh.getCell(2, 5).getContents();
		String ano_r = sh.getCell(3, 5).getContents();

		// Espero a que aparezca el pop up y lo cierro
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='as-login-modal-overlay social-popups-social-login']//*[@class='as-login-close as-login-icon-close-circled']"))).click();

		// Funcion para buscar vuelo
		buscarVuelo(origen, destino, dia_p, mes_p, ano_p, dia_r, mes_r, ano_r, driver);

		// Comprobación de que redirige a una nueva pagina con opciones de vuelo
		Assert.assertEquals(driver.getTitle(), "Despegar.com . Resultados de Vuelos");
	}

	@Test (priority = 2)
	public void caso2() throws BiffException, IOException {

		// Objetos para manipular el archivo Excel donde estan los parametros
		File fl = new File("Data.xls");
		Workbook wb = Workbook.getWorkbook(fl);
		Sheet sh = wb.getSheet("Caso 2");

		// Espero a que aparezca el pop up y lo cierro
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='as-login-modal-overlay social-popups-social-login']//*[@class='as-login-close as-login-icon-close-circled']"))).click();

		// Parametros
		String origen = sh.getCell(1, 0).getContents();
		String destino = sh.getCell(1, 1).getContents();

		//Obtengo parametros de fecha de salida
		String dia_p = sh.getCell(1, 4).getContents();
		String mes_p = sh.getCell(2, 4).getContents();
		String ano_p = sh.getCell(3, 4).getContents();

		//Obtengo parametros de fecha de regreso
		String dia_r = sh.getCell(1, 5).getContents();
		String mes_r = sh.getCell(2, 5).getContents();
		String ano_r = sh.getCell(3, 5).getContents();

		// Funcion para buscar vuelo
		buscarVuelo(origen, destino, dia_p, mes_p, ano_p, dia_r, mes_r, ano_r, driver);

		// Esperar a que este elemento aparezca asi se da tiempo para que carguen completos los resultados de la primera pestaña
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='toolbox-tabs-container results-cluster-container -show']")));
		
		// Hago un listado con los precios que se despliegan en la primera pestaña de resultados
		List<WebElement> precios = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@class='fare main-fare-big']//span[@class='amount price-amount']")));
		System.out.println("Cantidad de vuelos encontrados en primera pestaña: " + precios.size());
		
		// Defino un arreglo de enteros del tamaño de la cantidad de precios obtenidos en la pestaña
		int[] precios_enteros = new int[precios.size()];

		// Cada obtengo el precio en formato string de cada elemento del listado precios, le retir el "." y lo transformo en entero
		// esto con la intencion de poder compararlos entre ellos y determinar el mayor precio
		for (int i = 0; i < precios_enteros.length; i++) {
			precios_enteros[i] = Integer.parseInt(precios.get(i).getText().replace(".", ""));
		}

		// Variable que almacenara el precio mayor mientras se va recorriendo con un loop todo el arreglo precios_enteros
		int precio_mayor = precios_enteros[0];

		// En esta variable se almacenara el indice del elemento en el cual esta el precio mayor
		int indice_precio_mayor = 0;

		// A través de este loop se determina el precio mayor. Una vez determinado se almacena su indice
		// en la variable indice_precio_mayor
		for (int i = 0; i < precios_enteros.length; i++) {
			System.out.println("Precio " + (i+1) + ": " + precios_enteros[i]);
			if(precio_mayor<precios_enteros[i]) {
				indice_precio_mayor = i;
				precio_mayor = precios_enteros[i];

			}			
		}
		System.out.println("Precio mayor: " + precios.get(indice_precio_mayor).getText());
		System.out.println("Indice del precio mayor: "+indice_precio_mayor);

		// Se hace clic sobre el boton seleccionar
		driver.findElements(By.xpath("//*[@class='fare-couchmark-tooltip fare-box-container product-SEARCH ']//*[@class='btn-text' and text()='Seleccionar']")).get(indice_precio_mayor).click();

		// Verificar visibilidad de sector Pasajeros
		Assert.assertEquals(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='passengers-container']//*[@class='fch-title' and contains(text(),'Pasajeros')]"))).isDisplayed(), true);

		// Verificar visibilidad de sector Forma de Pago
		Assert.assertEquals(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='paymentDefinition']//*[@class='description' and contains(text(),'Forma de pago')]"))).isDisplayed(), true);

		// Verificar visibilidad de sector Datos para la emision de la factura
		Assert.assertEquals(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='invoiceDefinition']//*[@class='description fieldset-description' and contains(text(),'Datos para la emisión de la factura')]"))).isDisplayed(), true);

		// Verificar visibilidad de sector de informacion de contacto
		Assert.assertEquals(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='contact']//*[@class='description' and contains(text(),'¿En qué email querés recibir tus tickets electrónicos?')]"))).isDisplayed(), true);
		Assert.assertEquals(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='contact']//*[@class='description' and contains(text(),'¿A qué número podemos llamarte?')]"))).isDisplayed(), true);

		// Verificar si se despliega una nueva pagina
		Assert.assertEquals(driver.getTitle(), "Despegar.com - Checkout de compra");
	}

	@Test (priority = 3)
	public void caso3() {

		// Espero a que aparezca el pop up y lo cierro
		wait.until(ExpectedConditions.visibilityOfElementLocated(By.xpath("//*[@class='as-login-modal-overlay social-popups-social-login']//*[@class='as-login-close as-login-icon-close-circled']"))).click();

		// Clic en icono de alojamiento
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@title='Alojamientos']"))).click();

		// Ingresar el nombre de la ciudad
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@placeholder='Ingresá una ciudad, alojamiento o atracción']"))).sendKeys("Montevideo");

		// Selecciono la primera sugerencia del listado
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='ac-group-items']//*[@class='item -active']"))).click();

		// Debo cerciorarme que el listado de sugerencias de ciudades desaparezca antes de hacer clic en la fecha de entrada
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@class='ac-group-items']//*[@class='item -active']")));
		
		// Hago clic sobre el campo de fecha de entrada
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='input-tag sbox-checkin-date -sbox-3-no-radius-right' and @placeholder='Entrada']"))).click();

		// Creo un objeto Calendar inicializado con la fecha actual del sistema
		Calendar fecha_sistema = Calendar.getInstance();
		
		// Se añaden 10 dias a la fecha actual
		fecha_sistema.add(Calendar.DATE, 10);
		
		// Se obtienen los datos necesarios para localizar los elementos correspondientes a la fecha de entrada al alojamiento
		String dia_entrada = String.valueOf(fecha_sistema.get(Calendar.DATE));
		String mes_entrada = StringUtils.leftPad(String.valueOf(fecha_sistema.get(Calendar.MONTH)+1), 2, '0');
		String ano_entrada = String.valueOf(fecha_sistema.get(Calendar.YEAR));

		// Selecciono el dia de entrada	
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_dpmg2--wrapper _dpmg2--roundtrip _dpmg2--show-info _dpmg2--show' and @data-range='start']"
				+ "//*[@data-month='"+ano_entrada+"-"+mes_entrada+"']//*[@class='_dpmg2--dates']//span[text()='"+dia_entrada+"']"))).click();
		
		// Se añaden 3 días a la fecha de entrada
		fecha_sistema.add(Calendar.DATE, 3);

		// Se obtienen los datos necesarios para localizar los elementos correspondientes a la fecha de salida al alojamiento
		String dia_salida = String.valueOf(fecha_sistema.get(Calendar.DATE));
		String mes_salida = StringUtils.leftPad(String.valueOf(fecha_sistema.get(Calendar.MONTH)+1), 2, '0');
		String ano_salida = String.valueOf(fecha_sistema.get(Calendar.YEAR));

		// Selecciono el dia de regreso
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_dpmg2--wrapper _dpmg2--roundtrip _dpmg2--show-info _dpmg2--show _dpmg2--transition-displacement'"
				+ "and @data-range='end']//*[@data-month='"+ano_salida+"-"+mes_salida+"']//*[@class='_dpmg2--dates']//span[text()='"+dia_salida+"']"))).click();

		// Hago clic sobre el icono de las  camas para indicar la cantidad de huespedes
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='sbox-distri-inputs-container sbox-distribution-picker-wrapper sbox-distribution-picker-wrapper']"))).click();

		// Aumento la cantida de menores de 17 años de cero a uno
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_pnlpk-panel-scroll']//*[@class='_pnlpk-itemRow__item _pnlpk-stepper-minors -medium-down-to-lg']//*[@class='steppers-icon-right sbox-3-icon-plus']"))).click();

		// Defino un objeto select para seleccionar la edad del menor
		Select select_edad = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_pnlpk-panel-scroll']//*[@class='_pnlpk-minors-age-select-wrapper']//*[@class='select-tag']"))));
		select_edad.selectByVisibleText("12 años");

		// Hacer clic en Aplicar
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_pnlpk-apply-button sbox-3-btn-ghost _pnlpk-panel__button--link-right -md' and text()='Aplicar']"))).click();

		// Hacer clic en Buscar
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='searchbox-sbox-box-hotels searchbox-sbox-boxes sbox-ui-traditional hidden']//*[@class='sbox-button-container']//*[@class='btn-text' and text()='Buscar']"))).click();

		// Verificar que se despliega una nueva pagina con hoteles disponibles
		Assert.assertEquals(driver.getTitle(), "Alojamientos - Despegar.com");

		// Filtro los resultados obtenidos y selecciono los que tengan 5 estrellas
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='hf-dropdown-subcontent-wrapper dropdown-subcontent -show-more']//*[@data-ga-el='5']"))).click();

		// Espero a que este seleccionado el filtro de 5 estrellas
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='dropdown-item-container hf-checkbox-container analytics']//input[@type='checkbox' and @checked='checked']")));

		// Defino un objeto select para ordenar los resultados de menor a mayor precio
		// Esta operacion se realiza dentro de un try-catch debido a que durante el refrescamiento de los resultados cuando
		// se selecciona el filtrado por 5 estrellas, un elemento se superpone a resto del contenido mostrado en la pagina,
		// y ocasiona una StaleElementReferenceException
		try {
			Select ordenar = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='sorting']"))));
			ordenar.selectByVisibleText("Precio: menor a mayor");
		}
		catch(org.openqa.selenium.StaleElementReferenceException ex)
		{
			Select ordenar = new Select(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='sorting']"))));
			ordenar.selectByVisibleText("Precio: menor a mayor");
		}

		// Espero a que la opcion seleccionada es Precio: menor a mayor
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='select-tag trackable analytics hf-order-by ha-orderByCombo']//option[text()='Precio: menor a mayor']")));
		
		// Este elemento aparece mientras se actualizan los resultados producto del ordenamiento de menor a mayor con respecto al precio
		// se utiliza este comando para esperar que desaparezca y proseguir con los demas pasos del caso de prueba
		wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@id='fullLoader' and @class='hf-updating -show-updating']")));
		
		// Extraigo el nombre de la opcion con menor precio para confirmar la aparicion de la nueva pagina
		String nombre_hotel = "";
		try {
			nombre_hotel = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@class='results-cluster-container']//a[@class='upatracker']"))).get(0).getText();
		}
		catch(org.openqa.selenium.StaleElementReferenceException ex)
		{
			nombre_hotel = wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@class='results-cluster-container']//a[@class='upatracker']"))).get(0).getText();
		}

		// Clic en el hotel con menor precio
		try {
			wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@id='fullLoader' and @class='hf-updating']")));
			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@class='results-cluster-container']"))).get(0).click();
		}
		catch(org.openqa.selenium.StaleElementReferenceException ex)
		{
			wait.until(ExpectedConditions.invisibilityOfElementLocated(By.xpath("//*[@id='fullLoader' and @class='hf-updating']")));
			wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(By.xpath("//*[@class='results-cluster-container']"))).get(0).click();
		}

		// Cambiar el foco del driver a la nueva pestaña
		ArrayList<String> pestañas = new ArrayList<String> (driver.getWindowHandles());
		driver.switchTo().window(pestañas.get(1));

		// Verificacion de que se abrio una nueva pestaña con la informacion del hotel seleccionado
		Assert.assertEquals(driver.getTitle(), nombre_hotel);

		// Verificacion de que se despliega la disponibilidad de la habitacion
		Assert.assertEquals(wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@id='hf-price-box-container']"))).isDisplayed(), true);

		//Cierre de pestaña
		driver.close();
		driver.switchTo().window(pestañas.get(0));
	}

	@AfterMethod
	public void cerrarNavegador() {
		driver.close();
	}

	// ******** Funciones *********
	public String getNombreMes(String mes) {
		int numero_mes = Integer.parseInt(mes);
		String nombre_mes ="";
		switch (numero_mes) {
		case 1:
			nombre_mes = "Enero";
			break;
		case 2:
			nombre_mes = "Febrero";
			break;
		case 3:
			nombre_mes = "Marzo";
			break;
		case 4:
			nombre_mes = "Abril";
			break;
		case 5:
			nombre_mes = "Mayo";
			break;
		case 6:
			nombre_mes = "Junio";
			break;
		case 7:
			nombre_mes = "Julio";
			break;
		case 8:
			nombre_mes = "Agosto";
			break;
		case 9:
			nombre_mes = "Septiembre";
			break;
		case 10:
			nombre_mes = "Octubre";
			break;
		case 11:
			nombre_mes = "Noviembre";
			break;
		case 12:
			nombre_mes = "Diciembre";
			break;
		default:
			break;
		}
		return nombre_mes;
	}

	public void buscarVuelo(String origen, String destino, String dia_p, String mes_p, String ano_p, String dia_r, String mes_r, String ano_r, WebDriver driver) {

		WebDriverWait wait = new WebDriverWait(driver, 20);

		String nombre_mes_p = getNombreMes(mes_p);
		String nombre_mes_r = getNombreMes(mes_r);

		// Clic en icono de vuelos
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@title='Vuelos']"))).click();

		// Ingreso lugar de origen
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@placeholder='Ingresá desde dónde viajas']"))).clear();
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='input-tag sbox-main-focus sbox-bind-reference-flight-roundtrip-origin-input sbox-primary sbox-places-first places-inline' and @placeholder='Ingresá desde dónde viajas']"))).sendKeys(origen);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='ac-group-items']//*[@class='item -active']"))).click();

		// Ingreso lugar de destino
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@placeholder='Ingresá hacia dónde viajas']"))).sendKeys(destino);
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='ac-group-items']//*[@class='item -active']"))).click();

		// Clic en fecha de partida
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='input-tag sbox-bind-disable-start-date sbox-bind-value-start-date-segment-1 sbox-bind-reference-flight-start-date-input -sbox-3-no-radius-right' and @placeholder='Partida']"))).click();

		// Busco mes de partida
		while(!wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_dpmg2--wrapper _dpmg2--roundtrip _dpmg2--show-info _dpmg2--show']//*[@class='_dpmg2--months']//span[@class='_dpmg2--month-title-month' and text()='"+nombre_mes_p+"']"))).isDisplayed()) {
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_dpmg2--wrapper _dpmg2--roundtrip _dpmg2--show-info _dpmg2--show']//*[@class='_dpmg2--controlsWrapper']//*[@class='_dpmg2--controls-next']//*[@class='_dpmg2--icon-ico-arrow']"))).click();
		}

		// Selecciono el dia de partida		
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_dpmg2--wrapper _dpmg2--roundtrip _dpmg2--show-info _dpmg2--show' and @data-range='start']"
				+ "//*[@data-month='"+ano_p+"-"+StringUtils.leftPad(mes_p, 2, '0')+"']//*[@class='_dpmg2--dates']//span[text()='"+dia_p+"']"))).click();

		// Busco mes de regreso
		while(!wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_dpmg2--wrapper _dpmg2--roundtrip _dpmg2--show-info _dpmg2--show _dpmg2--transition-displacement']//*[@class='_dpmg2--months']//span[@class='_dpmg2--month-title-month' and text()='"+nombre_mes_r+"']"))).isDisplayed()) {
			wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_dpmg2--wrapper _dpmg2--roundtrip _dpmg2--show-info _dpmg2--show _dpmg2--transition-displacement']//*[@class='_dpmg2--controlsWrapper']//*[@class='_dpmg2--controls-next']//*[@class='_dpmg2--icon-ico-arrow']"))).click();
		}

		// Selecciono el dia de regreso
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='_dpmg2--wrapper _dpmg2--roundtrip _dpmg2--show-info _dpmg2--show _dpmg2--transition-displacement'"
				+ "and @data-range='end']//*[@data-month='"+ano_r+"-"+mes_r+"']//*[@class='_dpmg2--dates']//span[text()='"+dia_r+"']"))).click();
				
		// Hacer clic en Buscar
		wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//*[@class='sbox-button-default']//*[@class='btn-text' and text()='Buscar']"))).click();
	}
}