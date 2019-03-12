package models;

import javafx.scene.control.Label;
import javafx.scene.paint.Paint;

import java.io.Serializable;

public class SerializableLabel extends Label implements Serializable {
   private String textContent;
//   private Paint textFill;
   private String ID;
   private boolean textWrap;

   public SerializableLabel() {
      super();
   }

   public void initialize() {
      this.setText(textContent);
//      this.setTextFill(textFill);
      this.setWrapText(textWrap);
      this.setId(ID);
   }

   public void save() {
      this.textContent = this.getText();
//      this.textFill = this.getTextFill();
      this.ID = this.getId();
      this.textWrap = this.isWrapText();
   }
}
