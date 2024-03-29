import { crossIcon } from "./Icons";
import { useState } from "react";

function ErrorMessage(props) {
  const text = props.text;
  const [color, setColor] = useState("#ffc5c5");
  const close = props.close;
  const withLink = props.withLink || false;
  return (
    <div
      style={{
        border: "solid",
        borderWidth: 2,
        borderColor: "#ffc5c5",
        margin: "auto",
        paddingTop: "20px",
        paddingBottom: "20px",
        borderRadius: "25px",
        width: "350px",
        fontSize: "12px",
        backgroundColor: "rgba(161,94,94,0.58)",
        color: "#ffc5c5",
        marginTop: "15px",
        ...props.style,
      }}
    >
      <div
        style={{ display: "inline-block", marginLeft: "32px", width: "200px" }}
      >
        {typeof text === "string" || withLink === true ? text : "Generic error"}
      </div>
      <div
        style={{
          marginTop: "auto",
          marginBottom: "auto",
          display: "inline-block",
          float: "right",
          marginRight: "10px",
        }}
      >
        <a
          href={"/"}
          style={{ cursor: "pointer" }}
          onClick={(e) => {
            e.preventDefault();
            close();
          }}
          onMouseOver={() => setColor("#9f3232")}
          onMouseLeave={() => setColor("#ffc5c5")}
        >
          {crossIcon(color, 22)}
        </a>
      </div>
    </div>
  );
}

export default ErrorMessage;
