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


        binding.tvOperation.run {
            addTextChangedListener { charSequence ->
                if(canReplaceOperator(charSequence.toString())){
                    /* Snackbar.make(binding.root, "REEMPLAZABLE",
                         //el metodo setAnchorView es para poner el mensaje en el sitio que quieras
                         Snackbar.LENGTH_SHORT).setAnchorView(binding.linearlayoutTop).show()*/

                    val newStr = "$text".substring(0, text.length-2) +
                            text.substring(text.length-1)
                    text = newStr
                }
            }
        }
    } // llave de cierre del metodoOncreate

    private fun canReplaceOperator(charSequence: CharSequence): Boolean {
        if(charSequence.length < 2) return false

        val lastElement = charSequence[charSequence.length-1].toString()
        val penultimaElement = charSequence[charSequence.length-2].toString()
        return (lastElement == Contantes.OPERATOR_MULTI || lastElement == Contantes.OPERATOR_SUMA || lastElement == Contantes.OPERATOR_DIV)
                && (penultimaElement== Contantes.OPERATOR_MULTI || penultimaElement == Contantes.OPERATOR_SUMA ||
                penultimaElement == Contantes.OPERATOR_DIV || penultimaElement == Contantes.OPERATOR_RESTA)
    }

    // Metodo publico y basico para poder llamarlo desde la vista, por eso el parametro de view
    fun onClickButton(view: View){
        // Este mensaje se mostrar?? por consola que al hacer click sale el texto del bot??n
        //Log.i("click", (view as Button).text.toString())

        // Esta variable albergar?? el valor correspondiente a cada tecla
        val valueStr = (view as Button).text.toString()
        //Vamos a reducir c??digo del OnclickButton


        // Cuando en la vista se pulse el bot??n, aparacer?? su id (texto de la tecla)
        when(view.id){

            R.id.btnDelete ->{
                //Creamos variable longitud para saber el n??mero de d??gitos que est??n operando
                val length = binding.tvOperation.text.length

                // Ponemos este condicional pq sino, al llegar length a 0 y pulsar de nuevo peta la aplicaci??n
                if(length>0){
                    //Aqu?? creamos una variable nueva operacion que el texto que tenga la pantalla (tvOperation)
                    // hace que se coloque al final
                    val newOperation = binding.tvOperation.text.toString().substring(0, length-1)
                    // Al estar colocado al final y pulsar una vez, borra el ??ltimo caracter del string
                    binding.tvOperation.text = newOperation
                }
            }

            R.id.btnClear ->{
                    // Reiniciamos a un string vac??o tanto el tvOperation (superior) como tvResult (resultados)
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
                // .append metodo que a??ade el texto al final de lo que tengamos
                // el valor de cada tecla est?? almacenada en al variable valueStr
                binding.tvOperation.append(valueStr)
            }

        }
    }

    private fun addPoint(pointStr: String, operation: String) {
        // Si no contiene ese punto entonces agregalo
        if(!operation.contains(Contantes.POINT)){
            binding.tvOperation.append(pointStr)
        } else{
            val operator = getOperator(operation)
            var values = arrayOfNulls<String>(0)
            if(operator != Contantes.OPERATOR_NULL) {
                if (operator == Contantes.OPERATOR_RESTA) {
                    val index = operation.lastIndexOf(Contantes.OPERATOR_RESTA)
                    // Si el indice tiene dos operadores puede dividirse sino la operaci??n estar??a incompleta
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
                    if(numberOne.contains(Contantes.POINT) && !numberTwo.contains(Contantes.POINT)){
                        binding.tvOperation.append(pointStr)
                    }

                } else {
                    if(numberOne.contains(Contantes.POINT)){
                        binding.tvOperation.append(pointStr)
                    }
                }
            }
        }
    }

    // Este m??todo nos valida los casos en los que S?? es posible a??adir el operador (signo) que
    // estamos intentando agregar a la operacion actual
    private fun addOperator(operator: String, operation: String) {

            // Si la pantalla est?? vac??a es el ??ltimo elemento
            val lastElement = if(operation.isEmpty()) ""
            // Sino est?? vac??a se coloca en la ??ltima posici??n
            else operation.substring(operation.length-1)

            // Si el signo-operador es el de resta
            if(operator == Contantes.OPERATOR_RESTA){
                // Validamos el signo resta si est?? vac??o (puede ser numero negativo),
                    // o es ??ltimo elemento del primer operando (lo normal para operar, 5-3)
                        //o es diferente de punto, es decir, si es punto no agrega el signo
                if(operation.isEmpty() || lastElement != Contantes.OPERATOR_RESTA && lastElement !=Contantes.POINT){
                    binding.tvOperation.append(operator) //Evitamos un doble signo negativo
                }
            }else{
                // Con esto NO permitimos que se agrege un signo si la PartOne termina en punto
                if(!operation.isEmpty() && lastElement != Contantes.POINT) {
                    binding.tvOperation.append(operator) //x5 este caso es invalido
                }
            }
    }

    private fun tryResolver(operationRef: String, isFromResolve: Boolean) {

        if(operationRef.isEmpty()) return

        var operation = operationRef

        //Si tenemos un punto que esta al final de la pantalla, lo quitamos
        if(operation.contains(Contantes.POINT) && operation.lastIndexOf(Contantes.POINT) == operation.length-1){
            operation = operation.substring(0, operation.length-1)
        }

        val operator = getOperator(operation)

        var values = arrayOfNulls<String>(0)
        if(operator != Contantes.OPERATOR_NULL){
            if(operator == Contantes.OPERATOR_RESTA){
                val index = operation.lastIndexOf(Contantes.OPERATOR_RESTA)
                    // Si el indice tiene dos operadores puede dividirse sino la operaci??n estar??a incompleta
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
              //para asegurarnos que devuelve un Double y qu?? podr??a estar vac??o
              val numberOne = values[0]!!.toDouble()
              val numberTwo = values[1]!!.toDouble()

              // Para saber que n??meros ha pulsado el usuario y en que orden
              // operacion 582, numberOne es 5, y numberTwo es 8, seg??n las posiciones
              //Snackbar.make(binding.root, "1:$numberOne 2:$numberTwo", Snackbar.LENGTH_SHORT).show()

              // Si el metodo getResult devuelve un Double y tvResult un String, le ponemos el metodo .toString
              binding.tvResult.text = getResult(numberOne, operator, numberTwo).toString()

              //Si la pantalla grande no est?? vac??a, que el resultado lo ponga tambi??n en la pantallita superior
              if(binding.tvResult.text.isNotEmpty() && !isFromResolve){
                  binding.tvOperation.text = binding.tvResult.text
              }
          } catch (e:NumberFormatException){
                 if (isFromResolve)   showMessage()
          }

        }else{
            if( isFromResolve && operator != Contantes.OPERATOR_NULL) showMessage()
        }
    }

    private fun getOperator(operation: String): String {
        var operator = ""

        // Seleccionar y saber que tipo de operador ha seleccionado
        if (operation.contains(Contantes.OPERATOR_MULTI)){
            operator = Contantes.OPERATOR_MULTI
        } else if (operation.contains(Contantes.OPERATOR_DIV)){
            operator = Contantes.OPERATOR_DIV
        } else if (operation.contains(Contantes.OPERATOR_SUMA)){
            operator = Contantes.OPERATOR_SUMA
        } else {       // este ultimo caso o es un operador vac??o o es una resta
            operator = Contantes.OPERATOR_NULL
        }

        // Validaci??n de si tiene una operador de resta o uno vac??o
        // el lastIndexOf se queda con el ??ltimo operador que aparezca para asi permitir los negativos en la primer parte de la operacion
        // indice indexof = 0 hay un operador, pero si es mayor a 0, hay mas de un simbolo, por tanto, se ha metido negativos
        if(operator == Contantes.OPERATOR_NULL && operation.lastIndexOf(Contantes.OPERATOR_RESTA)>0){
            operator = Contantes.OPERATOR_RESTA
        }
        return operator
    }

    private fun getResult(PartOne: Double, operator: String, PartTwo: Double): Double{
        var result = 0.0

        when(operator){
            Contantes.OPERATOR_MULTI -> result = PartOne * PartTwo
            Contantes.OPERATOR_DIV -> result = PartOne / PartTwo
            Contantes.OPERATOR_RESTA -> result = PartOne - PartTwo
            Contantes.OPERATOR_SUMA -> result = PartOne + PartTwo
        }
        return result
    }

    private fun showMessage(){
        Snackbar.make(binding.root, getString(R.string.message_exp_incorrect),
            //el metodo setAnchorView es para poner el mensaje en el sitio que quieras
            Snackbar.LENGTH_SHORT).setAnchorView(binding.linearlayoutTop).show()
    }
}