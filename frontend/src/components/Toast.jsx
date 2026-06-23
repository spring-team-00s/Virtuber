function Toast({ toast, onClose }) {
  if (!toast) return null;

  return (
    <div className={`toast toast-${toast.type}`} role="alert">
      <span>{toast.text}</span>
      <button type="button" onClick={onClose}>
        닫기
      </button>
    </div>
  );
}

export default Toast;
