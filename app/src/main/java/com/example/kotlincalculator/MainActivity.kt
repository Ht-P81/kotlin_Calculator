package com.example.kotlincalculator

import android.icu.util.MeasureUnit.POINT
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import com.example.kotlincalculator.databinding.ActivityMainBinding
import com.google.android.material.snackbar.Snackbar
import java.lang.NumberFormatException

class MainActivity : AppCompatActivity() {

    //Creamos la variable binding para vincularlo despues del gradle
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.tvOperation.addTextChangedListener { charSequence ->
            if(canReplaceOperator(charSequence.toString())){
               /* Snackbar.make(binding.root, "REEMPLAZABLE",
                    //el metodo setAnchorView es para poner el mensaje en el sitio que quieras
                    Snackbar.LENGTH_SHORT).setAnchorView(binding.linearlayoutTop).show()*/
                val length = binding.tvOperation.text.length
                val newOperation = binding.tvOperation.text.toString().substring(0, length-2) +
                        binding.tvOperation.text.toString().substring(length-1)
                binding.tvOperation.text = newOperation
            }
        }
    } // llave de cierre del metodoOncreate

    private fun canReplaceOperator(charSequence: CharSequence): Boolean {
        if(charSequence.length < 2) return false

        val lastElement = charSequence[charSequence.length-1].toString()
        val penultimaElement = charSequence[charSequence.length-2].toString()
        return (lastElement == OPERATOR_MULTI || lastElement == OPERATOR_SUMA || lastElement == OPERATOR_DIV)
                && (penultimaElement== OPERATOR_MULTI || penultimaElement == OPERATOR_SUMA ||
                penultimaElement == OPERATOR_DIV || penultimaElement == OPERATOR_RESTA)
    }

    // Metodo publico y basico para poder llamarlo desde la vista, por eso el parametro de view
    fun onClickButton(view: View){
        // Este mensaje se mostrará por consola que al hacer click sale el texto del botón
        //Log.i("click", (view as Button).text.toString())

        // Esta variable albergará el valor correspondiente a cada tecla
        val valueStr = (view as Button).text.toString()

        // Cuando en la vista se pulse el botón, aparacerá su id (texto de la tecla)
        when(view.id){

            R.id.btnDelete ->{
                //Creamos variable longitud para saber el número de dígitos que están operando
                val length = binding.tvOperation.text.length

                // Ponemos este condicional pq sino, al llegar length a 0 y pulsar de nuevo peta la aplicación
                if(length>0){
                    //Aquí creamos una variable nueva operacion que el texto que tenga la pantalla (tvOperation)
                    // hace que se coloque al final
                    val newOperation = binding.tvOperation.text.toString().substring(0, length-1)
                    // Al estar colocado al final y pulsar una vez, borra el último caracter del string
                    binding.tvOperation.text = newOperation
                }
            }

            R.id.btnClear ->{
                    // Reiniciamos a un string vacío tanto el tvOperation (superior) como tvResult (resultados)
                    binding.tvOperation.setText("")
                    binding.tvResult.setText("")
            }
            R.id.btnResolve ->{
                    tryResolver(binding.tvOperation.text.toString(), true)
            }
            R.id.btnMultiplicar,
            R.id.btnDiv,
            R.id.btnSum,
            R.id.btnResta -> {
                // Si hay ya alguna operacion (un signo) que lo vaya calculando
                    tryResolver(binding.tvOperation.text.toString(), false)

                val operator = valueStr
                val operation = binding.tvOperation.text.toString()
                addOperator(operator, operation)

            }
            R.id.btnPoint ->{
                val operation = binding.tvOperation.text.toString()
                addPoint(valueStr, operation)
            }
            else ->{
                // .append metodo que añade el texto al final de lo que tengamos
                // el valor de cada tecla está almacenada en al variable valueStr
                binding.tvOperation.append(valueStr)
            }

        }
    }

    private fun addPoint(pointStr: String, operation: String) {
        // Si no contiene ese punto entonces agregalo
        if(!operation.contains(POINT)){
            binding.tvOperation.append(pointStr)
        } else{
            val operator = getOperator(operation)
            var values = arrayOfNulls<String>(0)
            if(operator != OPERATOR_NULL) {
                if (operator == OPERATOR_RESTA) {
                    val index = operation.lastIndexOf(OPERATOR_RESTA)
                    // Si el indice tiene dos operadores puede dividirse sino la operación estaría incompleta
                    if (index < operation.length - 1) {
                        values = arrayOfNulls(2)
                        //index es la posicion del simbolo - como operador
                        values[0] = operation.substring(0, index)
                        // Vamos a extraer desde ese indice (simbolo menos) hasta el final
                        values[1] = operation.substring(index + 1)
                    } else {
                        values = arrayOfNulls(1)
                        values[0] = operation.substring(0, index)
                    }
                } else {
                    values = operation.split(operator).toTypedArray()
                }
            }

            if(values.size > 0){
                val numberOne = values[0]!!
                if(values.size > 1){
                    val numberTwo = values[1]!!
                    // Si la primera parte de la operacion contiene punto, la segunda parte puede llevar punto
                    if(numberOne.contains(POINT) && !numberTwo.contains(POINT)){
                        binding.tvOperation.append(pointStr)
                    }

                } else {
                    if(numberOne.contains(POINT)){
                        binding.tvOperation.append(pointStr)
                    }
                }
            }
        }
    }

    // Este método nos valida los casos en los que SÍ es posible añadir el operador (signo) que
    // estamos intentando agregar a la operacion actual
    private fun addOperator(operator: String, operation: String) {

            // Si la pantalla está vacía es el último elemento
            val lastElement = if(operation.isEmpty()) ""
            // Sino está vacía se coloca en la última posición
            else operation.substring(operation.length-1)

            // Si el signo-operador es el de resta
            if(operator == OPERATOR_RESTA){
                // Validamos el signo resta si está vacío (puede ser numero negativo),
                    // o es último elemento del primer operando (lo normal para operar, 5-3)
                        //o es diferente de punto, es decir, si es punto no agrega el signo
                if(operation.isEmpty() || lastElement != OPERATOR_RESTA && lastElement !=POINT){
                    binding.tvOperation.append(operator) //Evitamos un doble signo negativo
                }
            }else{
                // Con esto NO permitimos que se agrege un signo si la PartOne termina en punto
                if(!operation.isEmpty() && lastElement != POINT) {
                    binding.tvOperation.append(operator) //x5 este caso es invalido
                }
            }
    }

    private fun tryResolver(operationRef: String, isFromResolve: Boolean) {

        if(operationRef.isEmpty()) return

        var operation = operationRef

        //Si tenemos un punto que esta al final de la pantalla, lo quitamos
        if(operation.contains(POINT) && operation.lastIndexOf(POINT) == operation.length-1){
            operation = operation.substring(0, operation.length-1)
        }

        val operator = getOperator(operation)

        var values = arrayOfNulls<String>(0)
        if(operator != OPERATOR_NULL){
            if(operator == OPERATOR_RESTA){
                val index = operation.lastIndexOf(OPERATOR_RESTA)
                    // Si el indice tiene dos operadores puede dividirse sino la operación estaría incompleta
                    if(index < operation.length-1){
                        values = arrayOfNulls(2)
                        //index es la posicion del simbolo - como operador
                        values[0] = operation.substring(0, index)
                        // Vamos a extraer desde ese indice (simbolo menos) hasta el final
                        values[1] = operation.substring(index+1)
                    } else{
                        values = arrayOfNulls(1)
                        values[0] = operation.substring(0, index)
                    }
            }else{
                values = operation.split(operator).toTypedArray()
            }

        }

        // Que tenga los dos (0 y 1), entonces que opere
        if(values.size > 1){
          try{
              //para asegurarnos que devuelve un Double y qué podría estar vacío
              val numberOne = values[0]!!.toDouble()
              val numberTwo = values[1]!!.toDouble()

              // Para saber que números ha pulsado el usuario y en que orden
              // operacion 582, numberOne es 5, y numberTwo es 8, según las posiciones
              //Snackbar.make(binding.root, "1:$numberOne 2:$numberTwo", Snackbar.LENGTH_SHORT).show()

              // Si el metodo getResult devuelve un Double y tvResult un String, le ponemos el metodo .toString
              binding.tvResult.text = getResult(numberOne, operator, numberTwo).toString()

              //Si la pantalla grande no está vacía, que el resultado lo ponga también en la pantallita superior
              if(binding.tvResult.text.isNotEmpty() && !isFromResolve){
                  binding.tvOperation.text = binding.tvResult.text
              }
          } catch (e:NumberFormatException){
                 if (isFromResolve)   showMessage()
          }

        }else{
            if( isFromResolve && operator != OPERATOR_NULL) showMessage()
        }
    }

    private fun getOperator(operation: String): String {
        var operator = ""

        // Seleccionar y saber que tipo de operador ha seleccionado
        if (operation.contains(OPERATOR_MULTI)){
            operator = OPERATOR_MULTI
        } else if (operation.contains(OPERATOR_DIV)){
            operator = OPERATOR_DIV
        } else if (operation.contains(OPERATOR_SUMA)){
            operator = OPERATOR_SUMA
        } else {       // este ultimo caso o es un operador vacío o es una resta
            operator = OPERATOR_NULL
        }

        // Validación de si tiene una operador de resta o uno vacío
        // el lastIndexOf se queda con el último operador que aparezca para asi permitir los negativos en la primer parte de la operacion
        // indice indexof = 0 hay un operador, pero si es mayor a 0, hay mas de un simbolo, por tanto, se ha metido negativos
        if(operator == OPERATOR_NULL && operation.lastIndexOf(OPERATOR_RESTA)>0){
            operator = OPERATOR_RESTA
        }
        return operator
    }

    private fun getResult(PartOne: Double, operator: String, PartTwo: Double): Double{
        var result = 0.0

        when(operator){
            OPERATOR_MULTI -> result = PartOne * PartTwo
            OPERATOR_DIV -> result = PartOne / PartTwo
            OPERATOR_RESTA -> result = PartOne - PartTwo
            OPERATOR_SUMA -> result = PartOne + PartTwo
        }
        return result
    }

    private fun showMessage(){
        Snackbar.make(binding.root, getString(R.string.message_exp_incorrect),
            //el metodo setAnchorView es para poner el mensaje en el sitio que quieras
            Snackbar.LENGTH_SHORT).setAnchorView(binding.linearlayoutTop).show()
    }

    companion object{
        const val OPERATOR_MULTI = "x"
        const val OPERATOR_DIV = "÷"
        const val OPERATOR_RESTA= "-"
        const val OPERATOR_SUMA = "+"
        const val OPERATOR_NULL= "null"
        const val POINT= "."
    }
}