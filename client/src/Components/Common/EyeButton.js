import React, { useState } from "react";
import { closedEyeIcon, eyeIcon } from "./Icons";

function EyeButton(props) {
  const [color, setColor] = useState("#FFFFFF");
  return (
    <div
      style={{ cursor: "pointer" }}
      onClick={() => props.onClick()}
      onMouseOver={() => setColor("#455156")}
      onMouseLeave={() => setColor("#FFFFFF")}
    >
      {!props.show ? closedEyeIcon(color, 20) : eyeIcon(color, 20)}
    </div>
  );
}

export default EyeButton;
